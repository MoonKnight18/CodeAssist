package org.gradle.internal.reflect;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.internal.reflect.validation.Severity;
import org.gradle.internal.reflect.validation.TypeValidationProblem;
import org.gradle.internal.reflect.validation.TypeValidationProblemRenderer;

import java.util.Optional;

import javax.annotation.Nullable;

public class DefaultTypeValidationContext extends ProblemRecordingTypeValidationContext {
    private final boolean reportCacheabilityProblems;
    private final ImmutableMap.Builder<String, Severity> problems = ImmutableMap.builder();

    public static DefaultTypeValidationContext withRootType(DocumentationRegistry documentationRegistry, Class<?> rootType, boolean cacheable) {
        return new DefaultTypeValidationContext(documentationRegistry, rootType, cacheable);
    }

    public static DefaultTypeValidationContext withoutRootType(DocumentationRegistry documentationRegistry, boolean reportCacheabilityProblems) {
        return new DefaultTypeValidationContext(documentationRegistry, null, reportCacheabilityProblems);
    }

    private DefaultTypeValidationContext(DocumentationRegistry documentationRegistry, @Nullable Class<?> rootType, boolean reportCacheabilityProblems) {
        super(documentationRegistry, rootType, Optional::empty);
        this.reportCacheabilityProblems = reportCacheabilityProblems;
    }

    @Override
    protected void recordProblem(TypeValidationProblem problem) {
        boolean onlyAffectsCacheableWork = problem.isOnlyAffectsCacheableWork();
        if (onlyAffectsCacheableWork && !reportCacheabilityProblems) {
            return;
        }
        problems.put(TypeValidationProblemRenderer.renderMinimalInformationAbout(problem), problem.getSeverity());
    }

    public ImmutableMap<String, Severity> getProblems() {
        return problems.build();
    }


}
