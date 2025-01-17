package com.tyron.builder.api.work;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tyron.builder.api.internal.exceptions.DefaultMultiCauseException;
import com.tyron.builder.api.internal.operations.BuildOperationRef;
import com.tyron.builder.api.internal.work.WorkerLeaseService;
import com.tyron.builder.api.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultAsyncWorkTracker implements AsyncWorkTracker {
    private final ListMultimap<BuildOperationRef, AsyncWorkCompletion> items = ArrayListMultimap.create();
    private final Set<BuildOperationRef> waiting = Sets.newHashSet();
    private final ReentrantLock lock = new ReentrantLock();
    private final WorkerLeaseService workerLeaseService;

    public DefaultAsyncWorkTracker(WorkerLeaseService workerLeaseService) {
        this.workerLeaseService = workerLeaseService;
    }

    @Override
    public void registerWork(BuildOperationRef operation, AsyncWorkCompletion workCompletion) {
        lock.lock();
        try {
            if (waiting.contains(operation)) {
                throw new IllegalStateException("Another thread is currently waiting on the completion of work for the provided operation");
            }
            items.put(operation, workCompletion);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void waitForCompletion(BuildOperationRef operation, ProjectLockRetention lockRetention) {
        final List<AsyncWorkCompletion> workItems;
        lock.lock();
        try {
            workItems = ImmutableList.copyOf(items.get(operation));
            startWaiting(operation, workItems);
        } finally {
            lock.unlock();
        }

        waitForAll(operation, workItems, lockRetention);
    }

    @Override
    public void waitForCompletion(BuildOperationRef operation, List<AsyncWorkCompletion> workItems, ProjectLockRetention lockRetention) {
        startWaiting(operation, workItems);
        waitForAll(operation, workItems, lockRetention);
    }

    private void waitForAll(BuildOperationRef operation, List<AsyncWorkCompletion> workItems, ProjectLockRetention lockRetention) {
        try {
            if (!workItems.isEmpty()) {
                waitForItemsAndGatherFailures(workItems, lockRetention);
            }
        } finally {
            stopWaiting(operation);
        }
    }

    private void waitForItemsAndGatherFailures(List<AsyncWorkCompletion> workItems, AsyncWorkTracker.ProjectLockRetention lockRetention) {
        switch (lockRetention) {
            case RETAIN_PROJECT_LOCKS:
                waitForItemsAndGatherFailures(workItems);
                return;
            case RELEASE_PROJECT_LOCKS:
                workerLeaseService.runAsIsolatedTask();
                waitForItemsAndGatherFailures(workItems);
                return;
            case RELEASE_AND_REACQUIRE_PROJECT_LOCKS:
                if (!hasWorkInProgress(workItems)) {
                    // All items are complete. Do not release project lock and simply collect failures.
                    waitForItemsAndGatherFailures(workItems);
                    return;
                }
                workerLeaseService.runAsIsolatedTask(() -> waitForItemsAndGatherFailures(workItems));
        }
    }

    private boolean hasWorkInProgress(List<AsyncWorkCompletion> workItems) {
        return workItems.stream()
                .anyMatch(workCompletion -> !workCompletion.isComplete());
    }

    @Override
    public boolean hasUncompletedWork(BuildOperationRef operation) {
        lock.lock();
        try {
            List<AsyncWorkCompletion> workItems = items.get(operation);
            for (AsyncWorkCompletion workCompletion : workItems) {
                if (!workCompletion.isComplete()) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void waitForItemsAndGatherFailures(Iterable<AsyncWorkCompletion> workItems) {
        // Release worker lease while waiting
        workerLeaseService.withoutLocks(Collections.singletonList(workerLeaseService.getCurrentWorkerLease()), () -> {
            final List<Throwable> failures = Lists.newArrayList();
            for (AsyncWorkCompletion item : workItems) {
                try {
                    item.waitForCompletion();
                } catch (Throwable t) {
                    if (Thread.currentThread().isInterrupted()) {
                        cancel(workItems);
                    }
                    failures.add(t);
                }
            }

            if (failures.size() > 0) {
                throw new DefaultMultiCauseException("There were failures while executing asynchronous work:", failures);
            }
        });
    }

    private void cancel(Iterable<AsyncWorkCompletion> workItems) {
        for (AsyncWorkCompletion workItem : workItems) {
            workItem.cancel();
        }
    }

    private void startWaiting(BuildOperationRef operation, List<AsyncWorkCompletion> workItems) {
        lock.lock();
        try {
            items.get(operation).removeAll(workItems);
            waiting.add(operation);
        } finally {
            lock.unlock();
        }
    }

    private void stopWaiting(BuildOperationRef operation) {
        lock.lock();
        try {
            waiting.remove(operation);
        } finally {
            lock.unlock();
        }
    }
}