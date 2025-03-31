package org.gradle.internal.logging.serializer;

import org.gradle.api.logging.LogLevel;
import org.gradle.internal.logging.events.StyledTextOutputEvent;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.internal.serialize.Decoder;
import org.gradle.internal.serialize.Encoder;
import org.gradle.internal.serialize.Serializer;

import java.util.List;

public class StyledTextOutputEventSerializer implements Serializer<StyledTextOutputEvent> {
    private final Serializer<LogLevel> logLevelSerializer;
    private final Serializer<List<StyledTextOutputEvent.Span>> spanSerializer;

    public StyledTextOutputEventSerializer(Serializer<LogLevel> logLevelSerializer, Serializer<List<StyledTextOutputEvent.Span>> spanSerializer) {
        this.logLevelSerializer = logLevelSerializer;
        this.spanSerializer = spanSerializer;
    }

    @Override
    public void write(Encoder encoder, StyledTextOutputEvent event) throws Exception {
        encoder.writeLong(event.getTimestamp());
        encoder.writeString(event.getCategory());
        logLevelSerializer.write(encoder, event.getLogLevel());
        if (event.getBuildOperationId() == null) {
            encoder.writeBoolean(false);
        } else {
            encoder.writeBoolean(true);
            encoder.writeSmallLong(event.getBuildOperationId().getId());
        }
        spanSerializer.write(encoder, event.getSpans());
    }

    @Override
    public StyledTextOutputEvent read(Decoder decoder) throws Exception {
        long timestamp = decoder.readLong();
        String category = decoder.readString();
        LogLevel logLevel = logLevelSerializer.read(decoder);
        OperationIdentifier buildOperationId = decoder.readBoolean() ? new OperationIdentifier(decoder.readSmallLong()) : null;
        List<StyledTextOutputEvent.Span> spans = spanSerializer.read(decoder);
        return new StyledTextOutputEvent(timestamp, category, logLevel, buildOperationId, spans);
    }
}

