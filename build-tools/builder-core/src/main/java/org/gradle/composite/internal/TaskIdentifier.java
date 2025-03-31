package org.gradle.composite.internal;


import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.internal.TaskInternal;

public interface TaskIdentifier {
    BuildIdentifier getBuildIdentifier();

    String getTaskPath();

    interface TaskBasedTaskIdentifier extends TaskIdentifier {
        TaskInternal getTask();
    }

    static TaskBasedTaskIdentifier of(BuildIdentifier buildIdentifier, TaskInternal task) {
        return new TaskBasedTaskIdentifier() {
            @Override
            public BuildIdentifier getBuildIdentifier() {
                return buildIdentifier;
            }

            @Override
            public TaskInternal getTask() {
                return task;
            }

            @Override
            public String getTaskPath() {
                return task.getPath();
            }
        };
    }

    static TaskIdentifier of(BuildIdentifier buildIdentifier, String taskPath) {
        return new TaskIdentifier() {
            @Override
            public BuildIdentifier getBuildIdentifier() {
                return buildIdentifier;
            }

            @Override
            public String getTaskPath() {
                return taskPath;
            }
        };
    }
}