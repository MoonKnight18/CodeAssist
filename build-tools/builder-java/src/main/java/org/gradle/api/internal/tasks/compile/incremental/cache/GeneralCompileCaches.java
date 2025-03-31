package org.gradle.api.internal.tasks.compile.incremental.cache;

import com.google.common.hash.HashCode;
import org.gradle.api.internal.tasks.compile.incremental.deps.ClassAnalysis;
import org.gradle.api.internal.tasks.compile.incremental.deps.ClassSetAnalysisData;
import org.gradle.cache.Cache;

/**
 * The build scoped compile caches.
 *
 * NOTE: This class cannot be renamed because it used to leak onto the public API
 * and some community plugins still depend on it in their byte code.
 */
public interface GeneralCompileCaches {
    Cache<HashCode, ClassSetAnalysisData> getClassSetAnalysisCache();
    Cache<HashCode, ClassAnalysis> getClassAnalysisCache();
}

