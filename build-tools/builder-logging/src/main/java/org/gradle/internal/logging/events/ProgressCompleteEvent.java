package org.gradle.internal.logging.events;

import org.gradle.internal.logging.events.OutputEvent;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.api.logging.LogLevel;

public class ProgressCompleteEvent extends OutputEvent {
    private final long timestamp;
    private final String status;
    private final OperationIdentifier progressOperationId;
    private final boolean failed;

    public ProgressCompleteEvent(OperationIdentifier progressOperationId, long timestamp, String status, boolean failed) {
        this.progressOperationId = progressOperationId;
        this.timestamp = timestamp;
        this.status = status;
        this.failed = failed;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFailed() {
        return failed;
    }

    @Override
    public String toString() {
        return "ProgressComplete (" + progressOperationId + ") " + status;
    }

    public OperationIdentifier getProgressOperationId() {
        return progressOperationId;
    }

    @Override
    public LogLevel getLogLevel() {
        return LogLevel.LIFECYCLE;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
