package org.gradle.internal.file.pattern;

public class AnyWildcardPatternStep implements PatternStep {
    @Override
    public boolean matches(String candidate) {
        return false;
    }
}
