package org.gradle.internal.operations;

public class BuildOperationInvocationException extends RuntimeException {
    public BuildOperationInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
