package org.gradle.api.internal.collections;

import org.gradle.api.Action;
import org.gradle.api.internal.provider.CollectionProviderInternal;
import org.gradle.api.internal.provider.ProviderInternal;

public interface PendingSource<T> {
    void realizePending();

    void realizePending(Class<?> type);

    boolean addPending(ProviderInternal<? extends T> provider);

    boolean removePending(ProviderInternal<? extends T> provider);

    boolean addPendingCollection(CollectionProviderInternal<T, ? extends Iterable<T>> provider);

    boolean removePendingCollection(CollectionProviderInternal<T, ? extends Iterable<T>> provider);

    void realizeExternal(ProviderInternal<? extends T> provider);

    void onRealize(Action<T> action);

    boolean isEmpty();

    int size();

    void clear();
}
