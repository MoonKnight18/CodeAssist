package com.tyron.builder.internal.watch.registry;


import com.tyron.builder.api.internal.snapshot.FileSystemLocationSnapshot;
import com.tyron.builder.api.internal.snapshot.SnapshotHierarchy;
import com.tyron.builder.internal.watch.WatchingNotSupportedException;

import javax.annotation.CheckReturnValue;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FileWatcherRegistry extends Closeable {

    boolean isWatchingAnyLocations();

    interface ChangeHandler {
        void handleChange(Type type, Path path);

        void stopWatchingAfterError();
    }

    enum Type {
        CREATED,
        MODIFIED,
        REMOVED,
        INVALIDATED,
        OVERFLOW
    }

    /**
     * Registers a watchable hierarchy.
     *
     * The watcher registry will only watch for changes in watchable hierarchies.
     *
     * @throws WatchingNotSupportedException when the native watchers can't be updated.
     */
    void registerWatchableHierarchy(File watchableHierarchy, SnapshotHierarchy root);

    /**
     * Updates the watchers after changes to the root.
     *
     * @throws WatchingNotSupportedException when the native watchers can't be updated.
     */
    void virtualFileSystemContentsChanged(Collection<FileSystemLocationSnapshot> removedSnapshots, Collection<FileSystemLocationSnapshot> addedSnapshots, SnapshotHierarchy root);

    /**
     * Updates the VFS and the watchers when the build started.
     *
     * For example, this method checks if watched hierarchies are where we left them after the previous build.
     */
    @CheckReturnValue
    SnapshotHierarchy updateVfsOnBuildStarted(SnapshotHierarchy root, WatchMode watchMode, List<File> unsupportedFileSystems);

    /**
     * Updates the VFS and the watchers when the build finished.

     * For example, this removes everything from the root which can't be kept after the current build finished.
     * This is anything which is not within a watchable hierarchy or in a cache directory.
     *
     * @return the snapshot hierarchy without snapshots which can't be kept till the next build.
     */
    @CheckReturnValue
    SnapshotHierarchy updateVfsOnBuildFinished(SnapshotHierarchy root, WatchMode watchMode, int maximumNumberOfWatchedHierarchies, List<File> unsupportedFileSystems);

    /**
     * Get statistics about the received changes.
     */
    FileWatchingStatistics getAndResetStatistics();

    /**
     * Configures debug logging.
     *
     * When enabled, {@link net.rubygrapefruit.platform.internal.jni.NativeLogger} will emit {@link java.util.logging.Level#FINEST} logs.
     */
    void setDebugLoggingEnabled(boolean debugLoggingEnabled);

    /**
     * Close the watcher registry. Stops watching without handling the changes.
     */
    @Override
    void close() throws IOException;

    interface FileWatchingStatistics {
        Optional<Throwable> getErrorWhileReceivingFileChanges();
        boolean isUnknownEventEncountered();
        int getNumberOfReceivedEvents();
        int getNumberOfWatchedHierarchies();
    }
}
