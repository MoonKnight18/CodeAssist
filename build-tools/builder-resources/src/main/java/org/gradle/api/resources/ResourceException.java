package org.gradle.api.resources;

import org.gradle.api.GradleException;
import org.gradle.internal.exceptions.Contextual;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * Generic resource exception that all other resource-related exceptions inherit from.
 */
@Contextual
public class ResourceException extends GradleException {
    private final URI location;

    public ResourceException() {
        location = null;
    }

    public ResourceException(String message) {
        super(message);
        location = null;
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
        location = null;
    }

    public ResourceException(URI location, String message) {
        super(message);
        this.location = location;
    }

    public ResourceException(URI location, String message, Throwable cause) {
        super(message, cause);
        this.location = location;
    }

    /**
     * Returns the location of the resource, if known.
     *
     * @return The location, or null if not known.
     */
    @Nullable
    public URI getLocation() {
        return location;
    }
}