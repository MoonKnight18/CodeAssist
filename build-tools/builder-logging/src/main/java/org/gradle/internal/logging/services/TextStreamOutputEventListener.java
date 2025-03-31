package org.gradle.internal.logging.services;

import org.gradle.internal.logging.events.OutputEvent;
import org.gradle.internal.logging.events.OutputEventListener;
import org.gradle.internal.logging.events.StyledTextOutputEvent;
import org.gradle.api.logging.LogLevel;
import org.gradle.internal.logging.events.LogLevelChangeEvent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link OutputEventListener} implementation which assigns log levels to text output
 * events that have no associated log level. This implementation is thread-safe.
 */
public class TextStreamOutputEventListener implements OutputEventListener {
    private final OutputEventListener listener;
    private AtomicReference<LogLevel> logLevel = new AtomicReference<LogLevel>(LogLevel.LIFECYCLE);

    public TextStreamOutputEventListener(OutputEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onOutput(OutputEvent event) {
        if (event instanceof StyledTextOutputEvent) {
            onTextEvent((StyledTextOutputEvent) event);
        } else if (event instanceof LogLevelChangeEvent) {
            onLogLevelChange((LogLevelChangeEvent) event);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void onLogLevelChange(LogLevelChangeEvent changeEvent) {
        logLevel.set(changeEvent.getNewLogLevel());
    }

    private void onTextEvent(StyledTextOutputEvent textOutputEvent) {
        if (textOutputEvent.getLogLevel() != null) {
            listener.onOutput(textOutputEvent);
        } else {
            listener.onOutput(textOutputEvent.withLogLevel(logLevel.get()));
        }
    }
}
