package org.gradle.internal.normalization.java.impl;

public class ArrayAnnotationValue extends AnnotationValue<AnnotationValue<?>[]> {

    public ArrayAnnotationValue(String name, AnnotationValue<?>[] value) {
        super(name, value);
    }
}