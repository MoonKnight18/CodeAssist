package com.tyron.builder.cache.internal;

import com.tyron.builder.api.internal.Factory;
import com.tyron.builder.api.internal.concurrent.ExecutorFactory;
import com.tyron.builder.api.internal.logging.progress.ProgressLogger;
import com.tyron.builder.api.internal.logging.progress.ProgressLoggerFactory;
import com.tyron.builder.api.internal.serialize.Serializer;
import com.tyron.builder.api.internal.time.Time;
import com.tyron.builder.api.internal.time.Timer;
import com.tyron.builder.api.util.GFileUtils;
import com.tyron.builder.cache.CacheBuilder;
import com.tyron.builder.cache.CacheOpenException;
import com.tyron.builder.cache.CleanupAction;
import com.tyron.builder.cache.FileLock;
import com.tyron.builder.cache.FileLockManager;
import com.tyron.builder.cache.LockOptions;
import com.tyron.builder.cache.PersistentIndexedCache;
import com.tyron.builder.cache.PersistentIndexedCacheParameters;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class DefaultPersistentDirectoryStore implements ReferencablePersistentCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPersistentDirectoryStore.class);

    public static final int CLEANUP_INTERVAL_IN_HOURS = 24;

    private final File dir;
    private final CacheBuilder.LockTarget lockTarget;
    private final LockOptions lockOptions;
    @Nullable
    private final CleanupAction cleanupAction;
    private final FileLockManager lockManager;
    private final ExecutorFactory executorFactory;
    private final String displayName;
    protected final File propertiesFile;
    private final File gcFile;
    private final ProgressLoggerFactory progressLoggerFactory;
    private CacheCoordinator cacheAccess;

    public DefaultPersistentDirectoryStore(
            File dir,
            @Nullable String displayName,
            CacheBuilder.LockTarget lockTarget,
            LockOptions lockOptions,
            @Nullable CleanupAction cleanupAction,
            FileLockManager fileLockManager,
            ExecutorFactory executorFactory,
            ProgressLoggerFactory progressLoggerFactory
    ) {
        this.dir = dir;
        this.lockTarget = lockTarget;
        this.lockOptions = lockOptions;
        this.cleanupAction = cleanupAction;
        this.lockManager = fileLockManager;
        this.executorFactory = executorFactory;
        this.propertiesFile = new File(dir, "cache.properties");
        this.gcFile = new File(dir, "gc.properties");
        this.progressLoggerFactory = progressLoggerFactory;
        this.displayName = displayName != null ? (displayName + " (" + dir + ")") : ("cache directory " + dir.getName() + " (" + dir + ")");
    }

    @Override
    public DefaultPersistentDirectoryStore open() {
        GFileUtils.mkdirs(dir);
        cacheAccess = createCacheAccess();
        try {
            cacheAccess.open();
        } catch (Throwable e) {
            throw new CacheOpenException(String.format("Could not open %s.", this), e);
        }

        return this;
    }

    private CacheCoordinator createCacheAccess() {
        return new DefaultCacheAccess(displayName, getLockTarget(), lockOptions, dir, lockManager, getInitAction(), getCleanupAction(), executorFactory);
    }

    private File getLockTarget() {
        switch (lockTarget) {
            case CacheDirectory:
            case DefaultTarget:
                return dir;
            case CachePropertiesFile:
                return propertiesFile;
            default:
                throw new IllegalArgumentException("Unsupported lock target: " + lockTarget);
        }
    }

    protected CacheInitializationAction getInitAction() {
        return new CacheInitializationAction() {
            @Override
            public boolean requiresInitialization(FileLock fileLock) {
                return false;
            }

            @Override
            public void initialize(FileLock fileLock) {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected CacheCleanupAction getCleanupAction() {
        return new Cleanup();
    }

    @Override
    public void close() {
        if (cacheAccess != null) {
            try {
                cacheAccess.close();
            } finally {
                cacheAccess = null;
            }
        }
    }

    @Override
    public File getBaseDir() {
        return dir;
    }

    @Override
    public Collection<File> getReservedCacheFiles() {
        return Arrays.asList(propertiesFile, gcFile, determineLockTargetFile(getLockTarget()));
    }

    // TODO: Duplicated in DefaultFileLockManager
    static File determineLockTargetFile(File target) {
        if (target.isDirectory()) {
            return new File(target, target.getName() + ".lock");
        } else {
            return new File(target.getParentFile(), target.getName() + ".lock");
        }
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public <K, V> PersistentIndexedCache<K, V> createCache(PersistentIndexedCacheParameters<K, V> parameters) {
        return cacheAccess.newCache(parameters);
    }

    @Override
    public <K, V> PersistentIndexedCache<K, V> createCache(String name, Class<K> keyType, Serializer<V> valueSerializer) {
        return cacheAccess.newCache(PersistentIndexedCacheParameters.of(name, keyType, valueSerializer));
    }

    @Override
    public <K, V> boolean cacheExists(PersistentIndexedCacheParameters<K, V> parameters) {
        return cacheAccess.cacheExists(parameters);
    }

    @Override
    public <T> T withFileLock(Factory<? extends T> action) {
        return cacheAccess.withFileLock(action);
    }

    @Override
    public void withFileLock(Runnable action) {
        cacheAccess.withFileLock(action);
    }

    @Override
    public <T> T useCache(Factory<? extends T> action) {
        return cacheAccess.useCache(action);
    }

    @Override
    public void useCache(Runnable action) {
        cacheAccess.useCache(action);
    }

    private class Cleanup implements CacheCleanupAction {
        @Override
        public boolean requiresCleanup() {
            if (cleanupAction != null) {
                if (!gcFile.exists()) {
                    GFileUtils.touch(gcFile);
                } else {
                    long duration = System.currentTimeMillis() - gcFile.lastModified();
                    long timeInHours = TimeUnit.MILLISECONDS.toHours(duration);
                    LOGGER.debug(DefaultPersistentDirectoryStore.this + " has last been fully cleaned up " + timeInHours + " hours ago");
                    return timeInHours >= CLEANUP_INTERVAL_IN_HOURS;
                }
            }
            return false;
        }

        @Override
        public void cleanup() {
            if (cleanupAction != null) {
                String description = "Cleaning " + getDisplayName();
                ProgressLogger progressLogger = progressLoggerFactory.newOperation(CacheCleanupAction.class).start(description, description);
                Timer timer = Time.startTimer();
                try {
                    cleanupAction.clean(DefaultPersistentDirectoryStore.this, new DefaultCleanupProgressMonitor(progressLogger));
                    GFileUtils.touch(gcFile);
                } finally {
                    LOGGER.debug(DefaultPersistentDirectoryStore.this + " cleaned up in " + timer.getElapsed());
                    progressLogger.completed();
                }
            }
        }
    }

}