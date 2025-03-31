package org.gradle.plugins.ide.internal;

import com.google.common.collect.Lists;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.project.ProjectState;
import org.gradle.api.internal.project.ProjectStateRegistry;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.internal.build.BuildState;
import org.gradle.util.internal.CollectionUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;

public class DefaultIdeArtifactRegistry implements IdeArtifactRegistry {
    private final IdeArtifactStore store;
    private final ProjectStateRegistry projectRegistry;
    private final FileOperations fileOperations;
    private final ProjectComponentIdentifier currentProject;

    public DefaultIdeArtifactRegistry(IdeArtifactStore store, ProjectStateRegistry projectRegistry, FileOperations fileOperations, DomainObjectContext domainObjectContext, BuildState currentBuild) {
        this.store = store;
        this.projectRegistry = projectRegistry;
        this.fileOperations = fileOperations;
        currentProject = currentBuild.getProjects().getProject(domainObjectContext.getProjectPath()).getComponentIdentifier();
    }

    @Override
    public void registerIdeProject(IdeProjectMetadata ideProjectMetadata) {
        store.put(currentProject, ideProjectMetadata);
    }

    @Nullable
    @Override
    public <T extends IdeProjectMetadata> T getIdeProject(Class<T> type, ProjectComponentIdentifier project) {
        ProjectState projectState = projectRegistry.stateFor(project);
        if (!projectState.getOwner().isImplicitBuild()) {
            // Do not include implicit builds in workspace
            for (IdeProjectMetadata ideProjectMetadata : store.get(project)) {
                if (type.isInstance(ideProjectMetadata)) {
                    return type.cast(ideProjectMetadata);
                }
            }
        }
        return null;
    }

    @Override
    public <T extends IdeProjectMetadata> List<Reference<T>> getIdeProjects(Class<T> type) {
        List<Reference<T>> result = Lists.newArrayList();
        for (ProjectState project : projectRegistry.getAllProjects()) {
            if (project.getOwner().isImplicitBuild()) {
                // Do not include implicit builds in workspace
                continue;
            }
            ProjectComponentIdentifier projectId = project.getComponentIdentifier();
            for (IdeProjectMetadata ideProjectMetadata : store.get(projectId)) {
                if (type.isInstance(ideProjectMetadata)) {
                    T metadata = type.cast(ideProjectMetadata);
                    result.add(new MetadataReference<T>(metadata, projectId));
                }
            }
        }
        return result;
    }

    @Override
    public FileCollection getIdeProjectFiles(final Class<? extends IdeProjectMetadata> type) {
        return fileOperations.immutableFiles(new Callable<List<FileCollection>>() {
            @Override
            public List<FileCollection> call() {
                return CollectionUtils.collect(
                    getIdeProjects(type),
                    new Transformer<FileCollection, Reference<?>>() {
                        @Override
                        public FileCollection transform(Reference<?> result) {
                            ConfigurableFileCollection singleton = fileOperations.configurableFiles(result.get().getFile());
                            singleton.builtBy(result.get().getGeneratorTasks());
                            return singleton;
                        }
                    });
            }
        });
    }

    private static class MetadataReference<T extends IdeProjectMetadata> implements Reference<T> {
        private final T metadata;
        private final ProjectComponentIdentifier projectId;

        MetadataReference(T metadata, ProjectComponentIdentifier projectId) {
            this.metadata = metadata;
            this.projectId = projectId;
        }

        @Override
        public T get() {
            return metadata;
        }

        @Override
        public ProjectComponentIdentifier getOwningProject() {
            return projectId;
        }

        @Override
        public void visitDependencies(TaskDependencyResolveContext context) {
            for (Task task : get().getGeneratorTasks()) {
                context.add(task);
            }
        }
    }
}