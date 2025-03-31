package org.gradle.internal.snapshot;

import static org.gradle.internal.file.FileMetadata.*;

import com.google.common.hash.HashCode;
import org.gradle.internal.file.FileType;
import org.gradle.internal.hash.Hashes;

import java.util.Optional;

/**
 * A snapshot of a missing file or a broken symbolic link or a named pipe.
 */
public class MissingFileSnapshot extends AbstractFileSystemLocationSnapshot implements FileSystemLeafSnapshot {
    private static final HashCode SIGNATURE = Hashes.signature(MissingFileSnapshot.class);

    public MissingFileSnapshot(String absolutePath, String name, AccessType accessType) {
        super(absolutePath, name, accessType);
    }

    public MissingFileSnapshot(String absolutePath, AccessType accessType) {
        this(absolutePath, PathUtil.getFileName(absolutePath), accessType);
    }

    @Override
    public FileType getType() {
        return FileType.Missing;
    }

    @Override
    public HashCode getHash() {
        return SIGNATURE;
    }

    @Override
    public boolean isContentAndMetadataUpToDate(FileSystemLocationSnapshot other) {
        return isContentUpToDate(other);
    }

    @Override
    public boolean isContentUpToDate(FileSystemLocationSnapshot other) {
        return other instanceof MissingFileSnapshot;
    }

    @Override
    public void accept(FileSystemLocationSnapshotVisitor visitor) {
        visitor.visitMissing(this);
    }

    @Override
    public <T> T accept(FileSystemLocationSnapshotTransformer<T> transformer) {
        return transformer.visitMissing(this);
    }

    public Optional<FileSystemNode> invalidate(VfsRelativePath targetPath, CaseSensitivity caseSensitivity, SnapshotHierarchy.NodeDiffListener diffListener) {
        diffListener.nodeRemoved(this);
        return Optional.empty();
    }

    @Override
    public String toString() {
        return String.format("%s/%s", super.toString(), getName());
    }
}