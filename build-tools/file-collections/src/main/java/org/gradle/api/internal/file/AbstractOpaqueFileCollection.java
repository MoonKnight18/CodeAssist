package org.gradle.api.internal.file;


import org.gradle.api.file.FileCollection;
import org.gradle.internal.Factory;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * A base class for {@link FileCollection} implementations that are not composed from other file collections.
 */
public abstract class AbstractOpaqueFileCollection extends AbstractFileCollection {
    public AbstractOpaqueFileCollection() {
    }

    public AbstractOpaqueFileCollection(Factory<PatternSet> patternSetFactory) {
        super(patternSetFactory);
    }

    /**
     * This is final - override {@link #getIntrinsicFiles()} instead.
     */
    @Override
    public final Set<File> getFiles() {
        return getIntrinsicFiles();
    }

    /**
     * This is final - override {@link #getIntrinsicFiles()} instead.
     */
    @Override
    public final Iterator<File> iterator() {
        return getIntrinsicFiles().iterator();
    }

    @Override
    protected void visitContents(FileCollectionStructureVisitor visitor) {
        visitor.visitCollection(OTHER, this);
    }

    abstract protected Set<File> getIntrinsicFiles();
}