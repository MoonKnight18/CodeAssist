package org.gradle.api.internal.provider;

import org.gradle.api.provider.HasMultipleValues;

import java.util.Collection;

public interface CollectionPropertyInternal<T, C extends Collection<T>> extends PropertyInternal<C>, HasMultipleValues<T>, CollectionProviderInternal<T, C> {
    Class<T> getElementType();

    @Override
    default int size() {
        return get().size();
    }
}