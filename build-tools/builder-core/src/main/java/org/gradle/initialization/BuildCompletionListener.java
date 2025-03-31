package org.gradle.initialization;

import org.gradle.internal.service.scopes.EventScope;
import org.gradle.internal.service.scopes.Scopes;

@EventScope(Scopes.Build.class)
public interface BuildCompletionListener {
    /**
     * Called after a build has completed and all its services and domain objects torn down. Implementations should take care to not use any such services.
     */
    void completed();
}
