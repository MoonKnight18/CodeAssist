package com.tyron.builder.internal.watch.registry.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tyron.builder.api.internal.snapshot.FileSystemLocationSnapshot;
import com.tyron.builder.api.internal.snapshot.SnapshotHierarchy;
import com.tyron.builder.internal.file.FileHierarchySet;
import com.tyron.builder.internal.watch.registry.FileWatcherProbeRegistry;
import com.tyron.builder.internal.watch.registry.FileWatcherUpdater;
import com.tyron.builder.internal.watch.registry.WatchMode;

import net.rubygrapefruit.platform.file.FileWatcher;
import net.rubygrapefruit.platform.internal.jni.WindowsFileEventFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Updater for hierarchical file watchers.
 *
 * For hierarchical watchers, we can use the registered watchable hierarchies as watched directories.
 * Build root directories are always watchable hierarchies.
 * Watching the build root directories is better since they are less likely to be deleted and
 * nearly no changes to the watched directories are necessary when running builds on the same project.
 *
 * To allow deleting the build root directories, we need to stop watching a build root directory if there are no more snapshots in the VFS inside,
 * since watched directories can't be deleted on Windows.
 *
 * The build root directories are discovered as included builds are encountered at the start of a build, and then they are removed when the build finishes.
 *
 * This is the lifecycle for the watchable hierarchies:
 * - During a build, there will be various calls to {@link FileWatcherUpdater#registerWatchableHierarchy(File, SnapshotHierarchy)},
 *   each call augmenting the collection. The watchers will be updated accordingly.
 * - When updating the watches, we watch watchable hierarchies registered for this build or old watched directories from previous builds instead of
 *   directories inside them.
 * - At the end of the build
 *   - stop watching the watchable directories with nothing to watch inside
 *   - remember the currently watched directories as old watched directories for the next build
 *   - remove everything that isn't watched from the virtual file system.
 */
public class HierarchicalFileWatcherUpdater extends AbstractFileWatcherUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(HierarchicalFileWatcherUpdater.class);

    private final FileWatcher fileWatcher;
    private final FileSystemLocationToWatchValidator locationToWatchValidator;
    private ImmutableSet<File> watchedHierarchies = ImmutableSet.of();

    public HierarchicalFileWatcherUpdater(
            FileWatcher fileWatcher,
            FileSystemLocationToWatchValidator locationToWatchValidator,
            FileWatcherProbeRegistry probeRegistry, WatchableHierarchies watchableHierarchies,
            MovedDirectoryHandler movedDirectoryHandler
    ) {
        super(probeRegistry, watchableHierarchies, movedDirectoryHandler);
        this.fileWatcher = fileWatcher;
        this.locationToWatchValidator = locationToWatchValidator;
    }

    @Override
    protected boolean handleVirtualFileSystemContentsChanged(Collection<FileSystemLocationSnapshot> removedSnapshots, Collection<FileSystemLocationSnapshot> addedSnapshots, SnapshotHierarchy root) {
        return watchableHierarchies.stream().anyMatch(watchableHierarchy -> {
            boolean hasSnapshotsToWatch = root.hasDescendantsUnder(watchableHierarchy.getPath());
            if (watchedFiles.contains(watchableHierarchy)) {
                // Need to stop watching this hierarchy
                return !hasSnapshotsToWatch;
            } else {
                // Need to start watching this hierarchy
                return hasSnapshotsToWatch;
            }
        });
    }

    @Override
    public SnapshotHierarchy updateVfsOnBuildFinished(SnapshotHierarchy root, WatchMode watchMode, int maximumNumberOfWatchedHierarchies, List<File> unsupportedFileSystems) {
        SnapshotHierarchy newRoot = super.updateVfsOnBuildFinished(root, watchMode, maximumNumberOfWatchedHierarchies, unsupportedFileSystems);
        LOGGER.info("Watched directory hierarchies: {}", watchedHierarchies);
        return newRoot;
    }

    @Override
    protected void updateWatchesOnChangedWatchedFiles(FileHierarchySet newWatchedFiles) {
        ImmutableSet<File> oldWatchedHierarchies = watchedHierarchies;
        ImmutableSet.Builder<File> watchedHierarchiesBuilder = ImmutableSet.builder();
        newWatchedFiles.visitRoots(absolutePath -> watchedHierarchiesBuilder.add(new File(absolutePath)));
        watchedHierarchies = watchedHierarchiesBuilder.build();

        if (watchedHierarchies.isEmpty()) {
            LOGGER.info("Not watching anything anymore");
        }

        List<File> hierarchiesToStopWatching = oldWatchedHierarchies.stream()
                .filter(oldWatchedHierarchy -> !watchedHierarchies.contains(oldWatchedHierarchy))
                .collect(ImmutableList.toImmutableList());
        if (!hierarchiesToStopWatching.isEmpty()) {
            if (!fileWatcher.stopWatching(hierarchiesToStopWatching)) {
                LOGGER.debug("Couldn't stop watching directories: {}", hierarchiesToStopWatching);
            }
        }

        List<File> hierarchiesToStartWatching = watchedHierarchies.stream()
                .filter(newWatchedHierarchy -> !oldWatchedHierarchies.contains(newWatchedHierarchy))
                .collect(ImmutableList.toImmutableList());
        if (!hierarchiesToStartWatching.isEmpty()) {
            hierarchiesToStartWatching.forEach(locationToWatchValidator::validateLocationToWatch);
            fileWatcher.startWatching(hierarchiesToStartWatching);
        }

        LOGGER.info("Watching {} directory hierarchies to track changes", watchedHierarchies.size());
    }

    @Override
    protected void startWatchingProbeDirectory(File probeDirectory) {
        // We already started watching the hierarchy.
    }

    @Override
    protected void stopWatchingProbeDirectory(File probeDirectory) {
        // We already stopped watching the hierarchy.
    }

    @Override
    protected WatchableHierarchies.Invalidator createInvalidator() {
        return (location, currentRoot) -> currentRoot.invalidate(location, SnapshotHierarchy.NodeDiffListener.NOOP);
    }

    public interface FileSystemLocationToWatchValidator {
        FileSystemLocationToWatchValidator NO_VALIDATION = location -> {
        };

        void validateLocationToWatch(File location);
    }
}
