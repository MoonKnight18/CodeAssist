package org.gradle.internal.logging.events;

import org.gradle.internal.logging.events.OutputEvent;
import org.gradle.api.logging.LogLevel;

public class UserInputResumeEvent extends OutputEvent {

    @Override
    public LogLevel getLogLevel() {
        return LogLevel.QUIET;
    }
}
