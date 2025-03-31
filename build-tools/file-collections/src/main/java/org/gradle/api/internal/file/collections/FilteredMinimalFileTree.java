package org.gradle.api.internal.file.collections;

import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;

public class FilteredMinimalFileTree implements MinimalFileTree, FileSystemMirroringFileTree, PatternFilterableFileTree {
    private final PatternSet patterns;
    private final FileSystemMirroringFileTree tree;

    public FilteredMinimalFileTree(PatternSet patterns, FileSystemMirroringFileTree tree) {
        this.patterns = patterns;
        this.tree = tree;
    }

    @Override
    public String getDisplayName() {
        return tree.getDisplayName();
    }

    public FileSystemMirroringFileTree getTree() {
        return tree;
    }

    public PatternSet getPatterns() {
        return patterns;
    }

    @Override
    public DirectoryFileTree getMirror() {
        DirectoryFileTree mirror = tree.getMirror();
        return mirror.filter(this.patterns);
    }

    @Override
    public MinimalFileTree filter(PatternFilterable patterns) {
        PatternSet filter = this.patterns.intersect();
        filter.copyFrom(patterns);
        return new FilteredMinimalFileTree(filter, tree);
    }

    @Override
    public void visitStructure(MinimalFileTreeStructureVisitor visitor, FileTreeInternal owner) {
        tree.visitStructure(new MinimalFileTreeStructureVisitor() {
            @Override
            public void visitFileTree(File root, PatternSet patterns, FileTreeInternal fileTree) {
                PatternSet intersect = patterns.intersect();
                intersect.copyFrom(FilteredMinimalFileTree.this.patterns);
                visitor.visitFileTree(root, intersect, owner);
            }

            @Override
            public void visitFileTreeBackedByFile(File file, FileTreeInternal fileTree, FileSystemMirroringFileTree sourceTree) {
                visitor.visitFileTreeBackedByFile(file, owner, FilteredMinimalFileTree.this);
            }
        }, owner);
    }

    @Override
    public void visit(FileVisitor visitor) {
        Spec<FileTreeElement> spec = patterns.getAsSpec();
        tree.visit(new FileVisitor() {
            @Override
            public void visitDir(FileVisitDetails dirDetails) {
                if (spec.isSatisfiedBy(dirDetails)) {
                    visitor.visitDir(dirDetails);
                }
            }

            @Override
            public void visitFile(FileVisitDetails fileDetails) {
                if (spec.isSatisfiedBy(fileDetails)) {
                    visitor.visitFile(fileDetails);
                }
            }
        });
    }
}