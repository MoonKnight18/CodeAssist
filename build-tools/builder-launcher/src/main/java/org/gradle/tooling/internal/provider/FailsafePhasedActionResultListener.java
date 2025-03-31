package org.gradle.tooling.internal.provider;

import org.gradle.internal.event.ListenerNotificationException;
import org.gradle.tooling.internal.protocol.PhasedActionResult;
import org.gradle.tooling.internal.protocol.PhasedActionResultListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener that will collect failures from the delegate listener and rethrow them in the right moment of the build.
 */
public class FailsafePhasedActionResultListener implements PhasedActionResultListener {
    private final PhasedActionResultListener delegate;
    private final List<Throwable> listenerFailures = new ArrayList<>();

    public FailsafePhasedActionResultListener(PhasedActionResultListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onResult(PhasedActionResult<?> event) {
        try {
            delegate.onResult(event);
        } catch (Throwable t) {
            listenerFailures.add(t);
        }
    }

    public void rethrowErrors() {
        if (!listenerFailures.isEmpty()) {
            throw new ListenerNotificationException(null, "One or more build phasedAction listeners failed with an exception.", listenerFailures);
        }
    }
}
