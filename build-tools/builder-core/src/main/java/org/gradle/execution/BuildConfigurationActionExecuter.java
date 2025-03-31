package org.gradle.execution;

import org.gradle.execution.plan.ExecutionPlan;
import org.gradle.api.internal.GradleInternal;

import java.util.List;


/**
 * Selects the tasks requested for a build.
 */
public interface BuildConfigurationActionExecuter {
    /**
     * Selects the tasks to execute, if any. This method is called before any other methods on this executer.
     */
    void select(GradleInternal gradle, ExecutionPlan plan);

    /**
     * registers actions allowing late customization of handled BuildConfigurationActions, if any. This method is called before any other methods on this executer.
     */
    void setTaskSelectors(List<? extends BuildConfigurationAction> taskSelectors);
}
