package org.gradle.api.internal.tasks.properties.annotations;


import static org.gradle.api.internal.tasks.properties.ModifierAnnotationCategory.OPTIONAL;

import com.google.common.collect.ImmutableSet;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.reflect.AnnotationCategory;
import org.gradle.internal.reflect.PropertyMetadata;
import org.gradle.api.internal.tasks.TaskDependencyContainer;
import org.gradle.api.internal.tasks.properties.BeanPropertyContext;
import org.gradle.api.internal.tasks.properties.PropertyValue;
import org.gradle.api.internal.tasks.properties.PropertyVisitor;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class NestedBeanAnnotationHandler implements PropertyAnnotationHandler {

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Nested.class;
    }

    @Override
    public ImmutableSet<? extends AnnotationCategory> getAllowedModifiers() {
        return ImmutableSet.of(OPTIONAL);
    }

    @Override
    public boolean isPropertyRelevant() {
        return true;
    }

    @Override
    public boolean shouldVisit(PropertyVisitor visitor) {
        return true;
    }

    @Override
    public void visitPropertyValue(String propertyName, PropertyValue value, PropertyMetadata propertyMetadata, PropertyVisitor visitor, BeanPropertyContext context) {
        Object nested;
        try {
            nested = unpackProvider(value.call());
        } catch (Exception e) {
            visitor.visitInputProperty(propertyName, new InvalidValue(e), false);
            return;
        }
        if (nested != null) {
            context.addNested(propertyName, nested);
        } else if (!propertyMetadata.isAnnotationPresent(Optional.class)) {
            visitor.visitInputProperty(propertyName, new AbsentValue(), false);
        }
    }

    @Nullable
    private static Object unpackProvider(@Nullable Object value) {
        // Only unpack one level of Providers, since Provider<Provider<>> is not supported - we don't need two levels of laziness.
        if (value instanceof Provider) {
            return ((Provider<?>) value).getOrNull();
        }
        return value;
    }

    private static class InvalidValue implements PropertyValue {
        private final Exception exception;

        public InvalidValue(Exception exception) {
            this.exception = exception;
        }

        @Nullable
        @Override
        public Object call() {
            throw UncheckedException.throwAsUncheckedException(exception);
        }

        @Nullable
        @Override
        public Object getUnprocessedValue() {
            return call();
        }

        @Override
        public TaskDependencyContainer getTaskDependencies() {
            // Ignore
            return TaskDependencyContainer.EMPTY;
        }

        @Override
        public void maybeFinalizeValue() {
            // Ignore
        }
    }

    private static class AbsentValue implements PropertyValue {
        @Nullable
        @Override
        public Object call() {
            return null;
        }

        @Nullable
        @Override
        public Object getUnprocessedValue() {
            return null;
        }

        @Override
        public TaskDependencyContainer getTaskDependencies() {
            // Ignore
            return TaskDependencyContainer.EMPTY;
        }

        @Override
        public void maybeFinalizeValue() {
            // Ignore
        }
    }
}