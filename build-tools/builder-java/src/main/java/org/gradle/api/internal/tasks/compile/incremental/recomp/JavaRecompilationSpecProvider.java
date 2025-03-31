package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.api.file.FileTree;
import org.gradle.internal.file.Deleter;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.work.FileChange;

import java.util.Collections;
import java.util.Set;

public class JavaRecompilationSpecProvider extends AbstractRecompilationSpecProvider {

    public JavaRecompilationSpecProvider(
            Deleter deleter,
            FileOperations fileOperations,
            FileTree sourceTree,
            boolean incremental,
            Iterable<FileChange> sourceFileChanges
    ) {
        super(deleter, fileOperations, sourceTree, sourceFileChanges, incremental);
    }

    @Override
    protected Set<String> getFileExtensions() {
        return Collections.singleton(".java");
    }

    @Override
    protected boolean isIncrementalOnResourceChanges(CurrentCompilation currentCompilation) {
        return currentCompilation.getAnnotationProcessorPath().isEmpty();
    }
}

