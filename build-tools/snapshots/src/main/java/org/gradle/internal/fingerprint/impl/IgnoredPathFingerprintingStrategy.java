package org.gradle.internal.fingerprint.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import org.gradle.internal.fingerprint.FileSystemLocationFingerprint;
import org.gradle.internal.fingerprint.FingerprintHashingStrategy;
import org.gradle.internal.fingerprint.hashing.FileSystemLocationSnapshotHasher;
import org.gradle.internal.snapshot.FileSystemLocationSnapshot;
import org.gradle.internal.snapshot.FileSystemLocationSnapshot.FileSystemLocationSnapshotVisitor;
import org.gradle.internal.snapshot.FileSystemSnapshot;
import org.gradle.internal.snapshot.MissingFileSnapshot;
import org.gradle.internal.snapshot.RegularFileSnapshot;
import org.gradle.internal.snapshot.SnapshotVisitResult;

import java.util.HashSet;
import java.util.Map;

/**
 * Fingerprint files ignoring the path.
 *
 * Ignores directories.
 */
public class IgnoredPathFingerprintingStrategy extends AbstractFingerprintingStrategy {
    public static final IgnoredPathFingerprintingStrategy DEFAULT = new IgnoredPathFingerprintingStrategy();
    public static final String IDENTIFIER = "IGNORED_PATH";
    public static final String IGNORED_PATH = "";

    private final FileSystemLocationSnapshotHasher normalizedContentHasher;

    public IgnoredPathFingerprintingStrategy(FileSystemLocationSnapshotHasher normalizedContentHasher) {
        super(IDENTIFIER, normalizedContentHasher);
        this.normalizedContentHasher = normalizedContentHasher;
    }

    @Override
    public String normalizePath(FileSystemLocationSnapshot snapshot) {
        return IGNORED_PATH;
    }

    private IgnoredPathFingerprintingStrategy() {
        this(FileSystemLocationSnapshotHasher.DEFAULT);
    }

    @Override
    public Map<String, FileSystemLocationFingerprint> collectFingerprints(FileSystemSnapshot roots) {
        ImmutableMap.Builder<String, FileSystemLocationFingerprint> builder = ImmutableMap.builder();
        HashSet<String> processedEntries = new HashSet<>();
        roots.accept(snapshot -> {
            snapshot.accept(new FileSystemLocationSnapshotVisitor() {
                @Override
                public void visitRegularFile(RegularFileSnapshot fileSnapshot) {
                    visitNonDirectoryEntry(snapshot);
                }

                @Override
                public void visitMissing(MissingFileSnapshot missingSnapshot) {
                    visitNonDirectoryEntry(snapshot);
                }

                private void visitNonDirectoryEntry(FileSystemLocationSnapshot snapshot) {
                    String absolutePath = snapshot.getAbsolutePath();
                    if (processedEntries.add(absolutePath)) {
                        HashCode normalizedContentHash = getNormalizedContentHash(snapshot, normalizedContentHasher);
                        if (normalizedContentHash != null) {
                            builder.put(absolutePath, IgnoredPathFileSystemLocationFingerprint.create(snapshot.getType(), normalizedContentHash));
                        }
                    }
                }
            });
            return SnapshotVisitResult.CONTINUE;
        });
        return builder.build();
    }

    @Override
    public FingerprintHashingStrategy getHashingStrategy() {
        return FingerprintHashingStrategy.SORT;
    }
}