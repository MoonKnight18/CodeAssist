package org.gradle.api.internal.changedetection.state;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import org.gradle.internal.file.archive.ZipEntry;
import org.gradle.internal.fingerprint.hashing.RegularFileSnapshotContext;
import org.gradle.internal.fingerprint.hashing.ResourceHasher;
import org.gradle.internal.fingerprint.hashing.ZipEntryContext;
import org.gradle.internal.hash.Hashes;


import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Caches the result of hashing regular files with a {@link ResourceHasher}.
 * It does not cache the result of hashing {@link ZipEntry}s.
 * It also caches the absence of a hash.
 */
public class CachingResourceHasher implements ResourceHasher {
    private final ResourceHasher delegate;
    private final ResourceSnapshotterCacheService resourceSnapshotterCacheService;
    private final HashCode delegateConfigurationHash;

    public CachingResourceHasher(ResourceHasher delegate, ResourceSnapshotterCacheService resourceSnapshotterCacheService) {
        this.delegate = delegate;
        this.resourceSnapshotterCacheService = resourceSnapshotterCacheService;
        Hasher hasher = Hashes.newHasher();
        delegate.appendConfigurationToHasher(hasher);
        this.delegateConfigurationHash = hasher.hash();
    }

    @Nullable
    @Override
    public HashCode hash(RegularFileSnapshotContext fileSnapshotContext) throws IOException {
        return resourceSnapshotterCacheService.hashFile(fileSnapshotContext, delegate, delegateConfigurationHash);
    }

    @Override
    public HashCode hash(ZipEntryContext zipEntryContext) throws IOException {
        return delegate.hash(zipEntryContext);
    }

    @Override
    public void appendConfigurationToHasher(Hasher hasher) {
        delegate.appendConfigurationToHasher(hasher);
    }
}