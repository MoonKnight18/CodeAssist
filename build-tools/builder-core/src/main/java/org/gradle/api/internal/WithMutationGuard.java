package org.gradle.api.internal;

/**
 * @see MutationGuard
 * @see MutationGuards#of(Object)
 */
public interface WithMutationGuard {
    MutationGuard getMutationGuard();
}