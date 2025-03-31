package org.gradle.internal.state;


public interface ManagedFactoryRegistry {
    /**
     * Looks up a {@link ManagedFactory} that can provide the given type.
     */
    ManagedFactory lookup(int id);
}