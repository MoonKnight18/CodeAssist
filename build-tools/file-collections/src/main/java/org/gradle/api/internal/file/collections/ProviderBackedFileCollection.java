package org.gradle.api.internal.file.collections;

import org.gradle.internal.Factory;
import org.gradle.api.internal.file.CompositeFileCollection;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.internal.file.PathToFileResolver;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.ValueSupplier;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.ProviderResolutionStrategy;
import org.gradle.api.tasks.util.PatternSet;

import java.util.function.Consumer;

public class ProviderBackedFileCollection extends CompositeFileCollection {
    private final ProviderInternal<?> provider;
    private final PathToFileResolver resolver;
    private final ProviderResolutionStrategy providerResolutionStrategy;

    public ProviderBackedFileCollection(ProviderInternal<?> provider, PathToFileResolver resolver, Factory<PatternSet> patternSetFactory, ProviderResolutionStrategy providerResolutionStrategy) {
        super(patternSetFactory);
        this.provider = provider;
        this.resolver = resolver;
        this.providerResolutionStrategy = providerResolutionStrategy;
    }

    @Override
    public String getDisplayName() {
        return "file collection";
    }

    @Override
    public void visitDependencies(TaskDependencyResolveContext context) {
        ValueSupplier.ValueProducer producer = provider.getProducer();
        if (producer.isKnown()) {
            producer.visitProducerTasks(context);
        } else {
            // Producer is unknown, so unpack the value
            UnpackingVisitor unpackingVisitor = new UnpackingVisitor(context::add, resolver, patternSetFactory);
            unpackingVisitor.add(providerResolutionStrategy.resolve(provider));
        }
    }

    @Override
    protected void visitChildren(Consumer<FileCollectionInternal> visitor) {
        UnpackingVisitor unpackingVisitor = new UnpackingVisitor(visitor, resolver, patternSetFactory);
        unpackingVisitor.add(providerResolutionStrategy.resolve(provider));
    }

    public ProviderInternal<?> getProvider() {
        return provider;
    }
}