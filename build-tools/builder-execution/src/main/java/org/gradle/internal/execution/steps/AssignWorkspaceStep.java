package org.gradle.internal.execution.steps;

import com.google.common.collect.ImmutableSortedMap;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.WorkValidationContext;
import org.gradle.internal.execution.history.ExecutionHistoryStore;
import org.gradle.internal.execution.workspace.WorkspaceProvider;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.snapshot.ValueSnapshot;

import java.io.File;
import java.util.Optional;

public class AssignWorkspaceStep<C extends IdentityContext, R extends Result> implements Step<C, R> {
    private final Step<? super WorkspaceContext, ? extends R> delegate;

    public AssignWorkspaceStep(Step<? super WorkspaceContext, ? extends R> delegate) {
        this.delegate = delegate;
    }

    @Override
    public R execute(UnitOfWork work, C context) {
        WorkspaceProvider workspaceProvider = work.getWorkspaceProvider();
        return workspaceProvider.withWorkspace(context.getIdentity().getUniqueId(), (workspace, history) -> delegate.execute(work, new WorkspaceContext() {
            @Override
            public Optional<String> getNonIncrementalReason() {
                return context.getNonIncrementalReason();
            }

            @Override
            public WorkValidationContext getValidationContext() {
                return context.getValidationContext();
            }

            @Override
            public ImmutableSortedMap<String, ValueSnapshot> getInputProperties() {
                return context.getInputProperties();
            }

            @Override
            public ImmutableSortedMap<String, CurrentFileCollectionFingerprint> getInputFileProperties() {
                return context.getInputFileProperties();
            }

            @Override
            public UnitOfWork.Identity getIdentity() {
                return context.getIdentity();
            }

            @Override
            public File getWorkspace() {
                return workspace;
            }

            @Override
            public Optional<ExecutionHistoryStore> getHistory() {
                return Optional.ofNullable(history);
            }
        }));
    }
}
