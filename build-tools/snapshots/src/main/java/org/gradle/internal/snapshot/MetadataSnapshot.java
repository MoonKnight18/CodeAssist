package org.gradle.internal.snapshot;

import org.gradle.internal.file.FileType;

/**
 * A snapshot where we know the metadata (i.e. the type).
 */
public interface MetadataSnapshot {

    MetadataSnapshot DIRECTORY = new MetadataSnapshot() {
        @Override
        public FileType getType() {
            return FileType.Directory;
        }

        @Override
        public FileSystemNode asFileSystemNode() {
            return PartialDirectoryNode.withoutKnownChildren();
        }
    };

    /**
     * The type of the file.
     */
    FileType getType();

    FileSystemNode asFileSystemNode();
}