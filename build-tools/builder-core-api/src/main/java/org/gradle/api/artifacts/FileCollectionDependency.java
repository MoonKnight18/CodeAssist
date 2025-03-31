package org.gradle.api.artifacts;

import org.gradle.api.file.FileCollection;

/**
 * A {@code FileCollectionDependency} is a {@link Dependency} on a collection of local files which are not stored in a
 * repository.
 */
public interface FileCollectionDependency extends SelfResolvingDependency {
    /**
     * Returns the files attached to this dependency.
     *
     * @since 3.3
     */
    FileCollection getFiles();
}
