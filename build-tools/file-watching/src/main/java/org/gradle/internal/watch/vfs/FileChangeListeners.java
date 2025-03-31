package org.gradle.internal.watch.vfs;

import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;
import org.gradle.internal.watch.registry.FileWatcherRegistry;

import java.nio.file.Path;

/**
 * Allows the registration of {@link FileChangeListener}s.
 *
 * We can't use listener manager directly, since the listeners will be created in
 * child scopes of the user home scope.
 */
@ServiceScope(Scopes.UserHome.class)
public interface FileChangeListeners {
    /**
     * Registers the listener with the build, the listener can be unregistered with {@link #removeListener(FileChangeListener)}.
     */
    void addListener(FileChangeListener listener);

    void removeListener(FileChangeListener listener);

    void broadcastChange(FileWatcherRegistry.Type type, Path path);

    void broadcastWatchingError();
}
