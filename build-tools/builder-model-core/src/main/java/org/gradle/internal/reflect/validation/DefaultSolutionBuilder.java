package org.gradle.internal.reflect.validation;

import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.problems.BaseSolution;
import org.gradle.problems.Solution;

import java.util.function.Supplier;

public class DefaultSolutionBuilder implements SolutionBuilder {
    private final DocumentationRegistry documentationRegistry;
    private final Supplier<String> shortDescription;
    private Supplier<String> longDescription = () -> null;
    private Supplier<String> documentationLink = () -> null;


    public DefaultSolutionBuilder(DocumentationRegistry documentationRegistry, Supplier<String> shortDescription) {
        this.documentationRegistry = documentationRegistry;
        this.shortDescription = shortDescription;
    }

    @Override
    public SolutionBuilder withLongDescription(Supplier<String> description) {
        this.longDescription = description;
        return this;
    }

    @Override
    public SolutionBuilder withDocumentation(String id, String section) {
        this.documentationLink = () -> documentationRegistry.getDocumentationFor(id, section);
        return this;
    }

    Supplier<Solution> build() {
        return () -> new BaseSolution(shortDescription, longDescription, documentationLink);
    }
}