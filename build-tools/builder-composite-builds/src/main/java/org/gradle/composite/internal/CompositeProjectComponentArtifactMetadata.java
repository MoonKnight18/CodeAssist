package org.gradle.composite.internal;

import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.component.local.model.LocalComponentArtifactMetadata;
import org.gradle.internal.component.model.IvyArtifactName;

import java.io.File;

public class CompositeProjectComponentArtifactMetadata implements LocalComponentArtifactMetadata, ComponentArtifactIdentifier, DisplayName {
    private final ProjectComponentIdentifier componentIdentifier;
    private final LocalComponentArtifactMetadata delegate;
    private final File file;

    public CompositeProjectComponentArtifactMetadata(ProjectComponentIdentifier componentIdentifier, LocalComponentArtifactMetadata delegate, File file) {
        this.componentIdentifier = componentIdentifier;
        this.delegate = delegate;
        this.file = file;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public LocalComponentArtifactMetadata getDelegate() {
        return delegate;
    }

    @Override
    public ProjectComponentIdentifier getComponentId() {
        return componentIdentifier;
    }

    @Override
    public ComponentArtifactIdentifier getId() {
        return this;
    }

    @Override
    public IvyArtifactName getName() {
        return delegate.getName();
    }

    @Override
    public ProjectComponentIdentifier getComponentIdentifier() {
        return componentIdentifier;
    }

    @Override
    public String getDisplayName() {
        return delegate.getId().getDisplayName();
    }

    @Override
    public String getCapitalizedDisplayName() {
        return Describables.of(delegate.getId()).getCapitalizedDisplayName();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return delegate.getBuildDependencies();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompositeProjectComponentArtifactMetadata)) {
            return false;
        }

        CompositeProjectComponentArtifactMetadata that = (CompositeProjectComponentArtifactMetadata) o;
        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
