package org.gradle.api.internal.project.taskfactory;

import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.specs.Specs;
import org.gradle.internal.instantiation.InstantiationScheme;
import org.gradle.internal.reflect.Instantiator;

import javax.annotation.Nullable;

/**
 * A {@link ITaskFactory} which determines task actions, inputs and outputs based on annotation attached to the task properties. Also provides some validation based on these annotations.
 */
public class AnnotationProcessingTaskFactory implements ITaskFactory {
    private final Instantiator instantiator;
    private final TaskClassInfoStore taskClassInfoStore;
    private final ITaskFactory taskFactory;

    public AnnotationProcessingTaskFactory(Instantiator instantiator, TaskClassInfoStore taskClassInfoStore, ITaskFactory taskFactory) {
        this.instantiator = instantiator;
        this.taskClassInfoStore = taskClassInfoStore;
        this.taskFactory = taskFactory;
    }

    @Override
    public ITaskFactory createChild(ProjectInternal project, InstantiationScheme instantiationScheme) {
        return new AnnotationProcessingTaskFactory(instantiator, taskClassInfoStore, taskFactory.createChild(project, instantiationScheme));
    }

    @Override
    public <S extends Task> S create(TaskIdentity<S> taskIdentity, @Nullable Object[] constructorArgs) {
        return process(taskFactory.create(taskIdentity, constructorArgs));
    }

    private <S extends Task> S process(S task) {
        TaskClassInfo taskClassInfo = taskClassInfoStore.getTaskClassInfo(task.getClass());

        for (TaskActionFactory actionFactory : taskClassInfo.getTaskActionFactories()) {
            ((TaskInternal) task).prependParallelSafeAction(actionFactory.create(instantiator));
        }

        // Enabled caching if task type is annotated with @CacheableTask
        if (taskClassInfo.isCacheable()) {
            task.getOutputs().cacheIf("Annotated with @CacheableTask", Specs.SATISFIES_ALL);
        }
        taskClassInfo.getReasonNotToTrackState()
            .ifPresent(task::doNotTrackState);

        return task;
    }
}
