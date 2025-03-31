package org.gradle.internal.taskgraph;

import org.gradle.internal.operations.BuildOperationType;

/**
 * Computing the task graph for the build tree based on the inputs and build configuration.
 */
public final class CalculateTreeTaskGraphBuildOperationType implements BuildOperationType<CalculateTreeTaskGraphBuildOperationType.Details, CalculateTreeTaskGraphBuildOperationType.Result> {
    public interface Details {
    }

    public interface Result {
    }

    private CalculateTreeTaskGraphBuildOperationType() {
    }
}