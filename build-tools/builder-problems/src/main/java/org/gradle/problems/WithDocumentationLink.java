package org.gradle.problems;

import java.util.Optional;

public interface WithDocumentationLink {
    default Optional<String> getDocumentationLink() {
        return Optional.empty();
    }
}