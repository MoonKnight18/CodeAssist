package org.gradle.api.internal.tasks;

import org.gradle.api.internal.tasks.properties.OutputFilePropertyType;
import org.gradle.api.tasks.TaskOutputFilePropertyBuilder;

public class DefaultTaskOutputFilePropertyRegistration extends AbstractTaskFilePropertyRegistration implements TaskOutputFilePropertyRegistration {
    private final OutputFilePropertyType outputFilePropertyType;

    public DefaultTaskOutputFilePropertyRegistration(StaticValue value, OutputFilePropertyType outputFilePropertyType) {
        super(value);
        this.outputFilePropertyType = outputFilePropertyType;
    }

    @Override
    public TaskOutputFilePropertyBuilder withPropertyName(String propertyName) {
        setPropertyName(propertyName);
        return this;
    }

    @Override
    public TaskOutputFilePropertyBuilder optional() {
        return optional(true);
    }

    @Override
    public TaskOutputFilePropertyBuilder optional(boolean optional) {
        setOptional(optional);
        return this;
    }

    @Override
    public OutputFilePropertyType getPropertyType() {
        return outputFilePropertyType;
    }

    @Override
    public String toString() {
        return getPropertyName() + " (Output)";
    }
}