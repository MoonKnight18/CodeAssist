package org.gradle.internal.execution.history.changes;

import org.gradle.api.InvalidUserDataException;

public interface InputFileChanges extends ChangeContainer {
    boolean accept(String propertyName, ChangeVisitor visitor);

    InputFileChanges EMPTY = new InputFileChanges() {

        @Override
        public boolean accept(ChangeVisitor visitor) {
            return true;
        }

        @Override
        public boolean accept(String propertyName, ChangeVisitor visitor) {
            throw new InvalidUserDataException("Cannot query incremental changes for property " + propertyName + ": No incremental properties declared.");
        }
    };
}