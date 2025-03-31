package org.gradle.internal.execution;

import org.gradle.api.file.FileCollection;
import org.gradle.api.Project;

import java.io.File;
import java.util.Set;

public interface BuildOutputCleanupRegistry {

    /**
     * Registers outputs to be cleaned up as {@link Project#files(Object...)}.
     */
    void registerOutputs(Object files);

    /**
     * Determines if an output file is owned by this build and therefore can be safely removed.
     *
     * A file is owned by the build if it is registered as an output directly or within a directory registered as an output.
     */
    boolean isOutputOwnedByBuild(File file);

    /**
     * Finalizes the registered build outputs.
     *
     * After this call, it is impossible to register more outputs.
     */
    void resolveOutputs();

    /**
     * Gets the set of registered outputs as file collections.
     */
    Set<FileCollection> getRegisteredOutputs();
}