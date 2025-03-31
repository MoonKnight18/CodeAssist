package org.gradle.api.internal.tasks.compile.incremental.analyzer;

import com.google.common.hash.HashCode;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.tasks.compile.incremental.deps.ClassAnalysis;
import org.gradle.cache.Cache;

public class CachingClassDependenciesAnalyzer implements ClassDependenciesAnalyzer {
    private final ClassDependenciesAnalyzer analyzer;
    private final Cache<HashCode, ClassAnalysis> cache;

    public CachingClassDependenciesAnalyzer(ClassDependenciesAnalyzer analyzer, Cache<HashCode, ClassAnalysis> cache) {
        this.analyzer = analyzer;
        this.cache = cache;
    }

    @Override
    public ClassAnalysis getClassAnalysis(final HashCode classFileHash, final FileTreeElement classFile) {
        return cache.get(classFileHash, () -> analyzer.getClassAnalysis(classFileHash, classFile));
    }
}

