package org.gradle.internal.execution.history.changes;


import org.gradle.work.ChangeType;

public enum ChangeTypeInternal {
    ADDED("has been added", ChangeType.ADDED),
    MODIFIED("has changed", ChangeType.MODIFIED),
    REMOVED("has been removed", ChangeType.REMOVED);

    private final String description;
    private final ChangeType publicType;

    ChangeTypeInternal(String description, ChangeType publicType) {
        this.description = description;
        this.publicType = publicType;
    }

    public String describe() {
        return description;
    }

    public ChangeType getPublicType() {
        return publicType;
    }
}