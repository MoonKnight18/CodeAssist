package com.tyron.builder.api.internal.tasks.compile.incremental.classpath;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.tyron.builder.api.file.FileVisitDetails;
import com.tyron.builder.api.file.FileVisitor;
import com.tyron.builder.api.internal.file.FileOperations;
import com.tyron.builder.api.internal.hash.FileHasher;
import com.tyron.builder.api.internal.hash.StreamHasher;
import com.tyron.builder.api.internal.tasks.compile.incremental.analyzer.ClassDependenciesAnalyzer;
import com.tyron.builder.api.internal.tasks.compile.incremental.deps.ClassAnalysis;
import com.tyron.builder.api.internal.tasks.compile.incremental.deps.ClassDependentsAccumulator;
import com.tyron.builder.api.internal.tasks.compile.incremental.deps.ClassSetAnalysisData;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class DefaultClassSetAnalyzer implements ClassSetAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClassSetAnalyzer.class);

    private final FileHasher fileHasher;
    private final StreamHasher hasher;
    private final ClassDependenciesAnalyzer analyzer;
    private final FileOperations fileOperations;

    public DefaultClassSetAnalyzer(FileHasher fileHasher, StreamHasher streamHasher, ClassDependenciesAnalyzer analyzer, FileOperations fileOperations) {
        this.fileHasher = fileHasher;
        this.hasher = streamHasher;
        this.analyzer = analyzer;
        this.fileOperations = fileOperations;
    }

    public ClassSetAnalysisData analyzeClasspathEntry(File classpathEntry) {
        return analyze(classpathEntry, true);
    }

    @Override
    public ClassSetAnalysisData analyzeOutputFolder(File outputFolder) {
        return analyze(outputFolder, false);
    }

    private ClassSetAnalysisData analyze(File classSet, boolean abiOnly) {
        final ClassDependentsAccumulator accumulator = new ClassDependentsAccumulator();
        try {
            visit(classSet, accumulator, abiOnly);
        } catch (Exception e) {
            accumulator.fullRebuildNeeded(classSet + " could not be analyzed for incremental compilation. See the debug log for more details");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not analyze " + classSet + " for incremental compilation", e);
            }
        }

        return accumulator.getAnalysis();
    }

    private void visit(File classpathEntry, ClassDependentsAccumulator accumulator, boolean abiOnly) {
        if ("jar".equals(FilenameUtils.getExtension(classpathEntry.getName()))) {
            fileOperations.zipTree(classpathEntry).visit(new JarEntryVisitor(accumulator, abiOnly));
        }
        if (classpathEntry.isDirectory()) {
            fileOperations.fileTree(classpathEntry).visit(new DirectoryEntryVisitor(accumulator, abiOnly));
        }
    }

    private abstract class EntryVisitor implements FileVisitor {
        private final ClassDependentsAccumulator accumulator;
        private final boolean abiOnly;

        public EntryVisitor(ClassDependentsAccumulator accumulator, boolean abiOnly) {
            this.accumulator = accumulator;
            this.abiOnly = abiOnly;
        }

        @Override
        public void visitDir(FileVisitDetails dirDetails) {
        }

        @Override
        public void visitFile(FileVisitDetails fileDetails) {
            if (!fileDetails.getName().endsWith(".class")) {
                return;
            }

            HashCode classFileHash = getHashCode(fileDetails);

            try {
                ClassAnalysis analysis = maybeStripToAbi(analyzer.getClassAnalysis(classFileHash, fileDetails));
                accumulator.addClass(analysis, classFileHash);
            } catch (Exception e) {
                accumulator.fullRebuildNeeded(fileDetails.getName() + " could not be analyzed for incremental compilation. See the debug log for more details");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Could not analyze " + fileDetails.getName() + " for incremental compilation", e);
                }
            }
        }

        private ClassAnalysis maybeStripToAbi(ClassAnalysis analysis) {
            if (abiOnly) {
                return new ClassAnalysis(analysis.getClassName(), ImmutableSet.of(), analysis.getAccessibleClassDependencies(), analysis.getDependencyToAllReason(), analysis.getConstants());
            } else {
                return analysis;
            }
        }

        protected abstract HashCode getHashCode(FileVisitDetails fileDetails);
    }

    private class JarEntryVisitor extends EntryVisitor {

        public JarEntryVisitor(ClassDependentsAccumulator accumulator, boolean abiOnly) {
            super(accumulator, abiOnly);
        }

        @Override
        protected HashCode getHashCode(FileVisitDetails fileDetails) {
            InputStream inputStream = fileDetails.open();
            try {
                return hasher.hash(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private class DirectoryEntryVisitor extends EntryVisitor {

        public DirectoryEntryVisitor(ClassDependentsAccumulator accumulator, boolean abiOnly) {
            super(accumulator, abiOnly);
        }

        @Override
        protected HashCode getHashCode(FileVisitDetails fileDetails) {
            return fileHasher.hash(fileDetails.getFile(), fileDetails.getSize(), fileDetails.getLastModified());
        }
    }

}
