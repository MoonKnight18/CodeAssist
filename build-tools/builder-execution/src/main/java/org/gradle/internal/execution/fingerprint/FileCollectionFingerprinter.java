package org.gradle.internal.execution.fingerprint;

import org.gradle.api.file.FileCollection;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.fingerprint.FileCollectionFingerprint;
import org.gradle.internal.snapshot.FileSystemLocationSnapshot;
import org.gradle.internal.snapshot.FileSystemSnapshot;
import org.gradle.api.tasks.FileNormalizer;

import org.jetbrains.annotations.Nullable;

public interface FileCollectionFingerprinter {
    /**
     * The type used to refer to this fingerprinter in the {@link FileCollectionFingerprinterRegistry}.
     */
    Class<? extends FileNormalizer> getRegisteredType();

    /**
     * Creates a fingerprint of the contents of the given collection.
     */
    CurrentFileCollectionFingerprint fingerprint(FileCollection files);

    /**
     * Creates a fingerprint from the snapshot of a file collection.
     */
    CurrentFileCollectionFingerprint fingerprint(FileSystemSnapshot snapshot, @Nullable FileCollectionFingerprint previousFingerprint);

    /**
     * Returns an empty fingerprint.
     */
    CurrentFileCollectionFingerprint empty();

    /**
     * Returns the normalized path to use for the given root
     */
    String normalizePath(FileSystemLocationSnapshot root);
}