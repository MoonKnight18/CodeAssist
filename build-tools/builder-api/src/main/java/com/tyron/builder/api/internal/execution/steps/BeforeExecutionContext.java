package com.tyron.builder.api.internal.execution.steps;

import com.tyron.builder.api.internal.execution.history.BeforeExecutionState;

import java.util.Optional;

public interface BeforeExecutionContext extends PreviousExecutionContext {
    /**
     * Returns the execution state before execution.
     * Empty if execution state was not observed before execution.
     */
    Optional<BeforeExecutionState> getBeforeExecutionState();
}
