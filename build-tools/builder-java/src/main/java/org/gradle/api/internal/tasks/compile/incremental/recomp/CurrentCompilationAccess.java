package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.api.Action;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.BuildOperationQueue;
import org.gradle.internal.operations.RunnableBuildOperation;
import org.gradle.api.internal.tasks.compile.incremental.classpath.ClassSetAnalyzer;
import org.gradle.api.internal.tasks.compile.incremental.deps.ClassSetAnalysisData;
import org.gradle.internal.time.Time;
import org.gradle.internal.time.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CurrentCompilationAccess {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentCompilationAccess.class);
    private final ClassSetAnalyzer classSetAnalyzer;
    private final BuildOperationExecutor buildOperationExecutor;
    private ClassSetAnalysisData classpathSnapshot;

    public CurrentCompilationAccess(ClassSetAnalyzer classSetAnalyzer, BuildOperationExecutor buildOperationExecutor) {
        this.classSetAnalyzer = classSetAnalyzer;
        this.buildOperationExecutor = buildOperationExecutor;
    }

    public ClassSetAnalysisData analyzeOutputFolder(File outputFolder) {
        Timer clock = Time.startTimer();
        ClassSetAnalysisData snapshot = classSetAnalyzer.analyzeOutputFolder(outputFolder);
        LOG.info("Class dependency analysis for incremental compilation took {}.", clock.getElapsed());
        return snapshot;
    }


    public ClassSetAnalysisData getClasspathSnapshot(final Iterable<File> entries) {
        if (classpathSnapshot == null) {
            Timer clock = Time.startTimer();
            classpathSnapshot = ClassSetAnalysisData.merge(doSnapshot(entries));
            LOG.info("Created classpath snapshot for incremental compilation in {}.", clock.getElapsed());
        }
        return classpathSnapshot;
    }

    private List<ClassSetAnalysisData> doSnapshot(Iterable<File> entries) {
        return snapshotAll(entries).stream()
                .map(CreateSnapshot::getSnapshot)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<CreateSnapshot> snapshotAll(final Iterable<File> entries) {
        final List<CreateSnapshot> snapshotOperations = new ArrayList<>();

        buildOperationExecutor.runAll((Action<BuildOperationQueue<CreateSnapshot>>) buildOperationQueue -> {
            for (File entry : entries) {
                CreateSnapshot operation = new CreateSnapshot(entry);
                snapshotOperations.add(operation);
                buildOperationQueue.add(operation);
            }
        });
        return snapshotOperations;
    }

    private class CreateSnapshot implements RunnableBuildOperation {
        private final File entry;
        private ClassSetAnalysisData snapshot;

        private CreateSnapshot(File entry) {
            this.entry = entry;
        }

        @Override
        public void run(BuildOperationContext context) {
            if (entry.exists()) {
                snapshot = classSetAnalyzer.analyzeClasspathEntry(entry);
            }
        }

        @Override
        public BuildOperationDescriptor.Builder description() {
            return BuildOperationDescriptor.displayName("Create incremental compile snapshot for " + entry);
        }

        @Nullable
        public ClassSetAnalysisData getSnapshot() {
            return snapshot;
        }
    }
}

