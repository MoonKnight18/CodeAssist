package org.gradle.problems;


import java.util.Optional;

public interface WithDescription {
    String getShortDescription();
    default Optional<String> getLongDescription() {
        return Optional.empty();
    }
}