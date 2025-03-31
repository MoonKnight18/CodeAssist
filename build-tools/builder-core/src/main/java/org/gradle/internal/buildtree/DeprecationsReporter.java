package org.gradle.internal.buildtree;

import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.problems.buildtree.ProblemReporter;

import java.io.File;
import java.util.function.Consumer;

public class DeprecationsReporter implements ProblemReporter {
    @Override
    public String getId() {
        return "deprecations";
    }

    @Override
    public void report(File reportDir, Consumer<? super Throwable> validationFailures) {
        Throwable failure = DeprecationLogger.getDeprecationFailure();
        if (failure != null) {
            validationFailures.accept(failure);
        }
    }
}
