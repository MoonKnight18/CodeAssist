package org.gradle.internal.reflect.annotations;


import com.google.common.collect.ImmutableMap;
import org.gradle.internal.reflect.AnnotationCategory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface PropertyAnnotationMetadata extends Comparable<PropertyAnnotationMetadata> {
    Method getMethod();

    String getPropertyName();

    boolean isAnnotationPresent(Class<? extends Annotation> annotationType);

    ImmutableMap<AnnotationCategory, Annotation> getAnnotations();
}