package org.gradle.api;

import org.jetbrains.annotations.NotNull;

/**
 * Performs some actions against objects of of type T
 *
 * @param <T> the type of object which this action accepts
 */
public interface Action<T> {

    /**
     * Performs this action against the given object
     *
     * @param t The object to perform the action on.
     */
    void execute(@NotNull T t);

    default void invoke(String name, Object[] args) {
        throw new UnsupportedOperationException();
    }
}
