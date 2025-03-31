package org.gradle.internal.execution.fingerprint.impl;

import org.gradle.internal.execution.fingerprint.FileNormalizationSpec;
import org.gradle.internal.fingerprint.DirectorySensitivity;
import org.gradle.internal.fingerprint.LineEndingSensitivity;
import org.gradle.api.tasks.FileNormalizer;

import java.util.Objects;

public class DefaultFileNormalizationSpec implements FileNormalizationSpec {
    private final Class<? extends FileNormalizer> normalizer;
    private final DirectorySensitivity directorySensitivity;
    private final LineEndingSensitivity lineEndingSensitivity;

    private DefaultFileNormalizationSpec(Class<? extends FileNormalizer> normalizer, DirectorySensitivity directorySensitivity, LineEndingSensitivity lineEndingSensitivity) {
        this.normalizer = normalizer;
        this.directorySensitivity = directorySensitivity;
        this.lineEndingSensitivity = lineEndingSensitivity;
    }

    @Override
    public Class<? extends FileNormalizer> getNormalizer() {
        return normalizer;
    }

    @Override
    public DirectorySensitivity getDirectorySensitivity() {
        return directorySensitivity;
    }

    @Override
    public LineEndingSensitivity getLineEndingNormalization() {
        return lineEndingSensitivity;
    }

    public static FileNormalizationSpec from(Class<? extends FileNormalizer> normalizer, DirectorySensitivity directorySensitivity, LineEndingSensitivity lineEndingSensitivity) {
        return new DefaultFileNormalizationSpec(normalizer, directorySensitivity, lineEndingSensitivity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultFileNormalizationSpec that = (DefaultFileNormalizationSpec) o;
        return normalizer.equals(that.normalizer) &&
               directorySensitivity == that.directorySensitivity &&
               lineEndingSensitivity == that.lineEndingSensitivity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizer, directorySensitivity, lineEndingSensitivity);
    }
}