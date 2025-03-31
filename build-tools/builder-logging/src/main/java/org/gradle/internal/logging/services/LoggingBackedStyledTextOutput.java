package org.gradle.internal.logging.services;

import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.AbstractLineChoppingStyledTextOutput;
import org.gradle.internal.logging.events.OutputEventListener;
import org.gradle.internal.logging.events.StyledTextOutputEvent;
import org.gradle.internal.operations.CurrentBuildOperationRef;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.internal.time.Clock;
import org.gradle.api.logging.LogLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link StyledTextOutput} implementation which generates events of type {@link
 * StyledTextOutputEvent}. This implementation is not thread-safe.
 */
public class LoggingBackedStyledTextOutput extends AbstractLineChoppingStyledTextOutput {
    private final OutputEventListener listener;
    private final String category;
    private final LogLevel logLevel;
    private final Clock clock;
    private final StringBuilder buffer = new StringBuilder();
    private List<StyledTextOutputEvent.Span> spans = new ArrayList<>();
    private Style style = Style.Normal;

    public LoggingBackedStyledTextOutput(OutputEventListener listener, String category, LogLevel logLevel, Clock clock) {
        this.listener = listener;
        this.category = category;
        this.logLevel = logLevel;
        this.clock = clock;
    }

    @Override
    protected void doStyleChange(Style style) {
        if (buffer.length() > 0) {
            spans.add(new StyledTextOutputEvent.Span(this.style, buffer.toString()));
            buffer.setLength(0);
        }
        this.style = style;
    }

    @Override
    protected void doLineText(CharSequence text) {
        buffer.append(text);
    }

    @Override
    protected void doEndLine(CharSequence endOfLine) {
        buffer.append(endOfLine);
        spans.add(new StyledTextOutputEvent.Span(this.style, buffer.toString()));
        buffer.setLength(0);
        OperationIdentifier buildOperationId = CurrentBuildOperationRef.instance().getId();
        listener.onOutput(new StyledTextOutputEvent(clock.getCurrentTime(), category, logLevel, buildOperationId, spans));
        spans = new ArrayList<StyledTextOutputEvent.Span>();
    }
}
