package org.gradle.internal.execution.history.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.gradle.internal.execution.history.BeforeExecutionState;
import org.gradle.internal.execution.history.OverlappingOutputs;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.snapshot.FileSystemSnapshot;
import org.gradle.internal.snapshot.ValueSnapshot;
import org.gradle.internal.snapshot.impl.ImplementationSnapshot;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DefaultBeforeExecutionState extends AbstractInputExecutionState<CurrentFileCollectionFingerprint> implements BeforeExecutionState {
    @Nullable
    private final OverlappingOutputs detectedOutputOverlaps;
    private final ImmutableSortedMap<String, FileSystemSnapshot> outputFileLocationSnapshots;

    public DefaultBeforeExecutionState(
            ImplementationSnapshot implementation,
            ImmutableList<ImplementationSnapshot> additionalImplementations,
            ImmutableSortedMap<String, ValueSnapshot> inputProperties,
            ImmutableSortedMap<String, CurrentFileCollectionFingerprint> inputFileProperties,
            ImmutableSortedMap<String, FileSystemSnapshot> outputFileLocationSnapshots,
            @Nullable OverlappingOutputs detectedOutputOverlaps
    ) {
        super(
                implementation,
                additionalImplementations,
                inputProperties,
                inputFileProperties
        );
        this.outputFileLocationSnapshots = outputFileLocationSnapshots;
        this.detectedOutputOverlaps = detectedOutputOverlaps;
    }

    @Override
    public ImmutableSortedMap<String, FileSystemSnapshot> getOutputFileLocationSnapshots() {
        return outputFileLocationSnapshots;
    }

    @Override
    public Optional<OverlappingOutputs> getDetectedOverlappingOutputs() {
        return Optional.ofNullable(detectedOutputOverlaps);
    }
}