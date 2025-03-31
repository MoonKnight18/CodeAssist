package org.gradle.cache.internal;

import org.gradle.cache.CrossProcessCacheAccess;

import java.io.Closeable;

public abstract class AbstractCrossProcessCacheAccess implements CrossProcessCacheAccess, Closeable {
    /**
     * Opens this cache access instance when the cache is opened. State lock is held while this method is called.
     */
    public abstract void open();

    /**
     * Closes this cache access instance when the cache is opened. State lock is held while this method is called.
     */
    @Override
    public abstract void close();
}