package org.gradle.internal.build;

import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.project.ProjectState;
import org.gradle.tooling.provider.model.internal.ToolingModelScope;

/**
 * Coordinates the building of tooling models.
 */
public interface BuildToolingModelController {
    /**
     * Returns the mutable model, configuring if necessary.
     */
    GradleInternal getConfiguredModel();

    ToolingModelScope locateBuilderForTarget(String modelName, boolean param);

    ToolingModelScope locateBuilderForTarget(ProjectState target, String modelName, boolean param);
}