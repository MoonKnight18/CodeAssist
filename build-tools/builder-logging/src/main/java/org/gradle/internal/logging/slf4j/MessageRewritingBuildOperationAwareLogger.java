package org.gradle.internal.logging.slf4j;

import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.api.logging.LogLevel;

class MessageRewritingBuildOperationAwareLogger extends BuildOperationAwareLogger {

    private final BuildOperationAwareLogger delegate;
    private final ContextAwareTaskLogger.MessageRewriter messageRewriter;

    MessageRewritingBuildOperationAwareLogger(BuildOperationAwareLogger delegate, ContextAwareTaskLogger.MessageRewriter messageRewriter) {
        this.delegate = delegate;
        this.messageRewriter = messageRewriter;
    }

    @Override
    void log(LogLevel logLevel, Throwable throwable, String message, OperationIdentifier operationIdentifier) {
        final String rewrittenMessage = messageRewriter.rewrite(logLevel, message);
        if (rewrittenMessage == null) {
            return;
        }
        delegate.log(logLevel, throwable, rewrittenMessage, operationIdentifier);
    }

    @Override
    boolean isLevelAtMost(LogLevel level) {
        return delegate.isLevelAtMost(level);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
