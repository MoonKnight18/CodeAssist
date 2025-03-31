package org.gradle.api.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.tasks.ConventionTask;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Factory;

import java.util.Set;
import java.util.function.Predicate;

public class SourceTask extends ConventionTask implements PatternFilterable {

    private ConfigurableFileCollection sourceFiles = getProject().getObjects().fileCollection();
    private final PatternFilterable patternSet;

    public SourceTask() {
        patternSet = getPatternSetFactory().create();
    }

    @Internal
    protected Factory<PatternSet> getPatternSetFactory() {
        return getServices().getFactory(PatternSet.class);
    }

    @InputFiles
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public FileTree getSource() {
        return sourceFiles.getAsFileTree().matching(patternSet);
    }

    /**
     * Sets the source for this task.
     *
     * @param source The source.
     * @since 4.0
     */
    public void setSource(FileTree source) {
        setSource((Object) source);
    }

    /**
     * Sets the source for this task. The given source object is evaluated as per
     * {@link Project#files(Object...)}.
     *
     * @param source The source.
     */
    public void setSource(Object source) {
        sourceFiles = getProject().getObjects().fileCollection().from(source);
    }

    /**
     * Adds some source to this task. The given source objects will be evaluated as per
     * {@link Project#files(Object...)}.
     *
     * @param sources The source to add
     * @return this
     */
    public SourceTask source(Object... sources) {
        sourceFiles.from(sources);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask include(String... includes) {
        patternSet.include(includes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask include(Iterable<String> includes) {
        patternSet.include(includes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask include(Spec<FileTreeElement> includeSpec) {
        patternSet.include(includeSpec);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask exclude(String... excludes) {
        patternSet.exclude(excludes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask exclude(Iterable<String> excludes) {
        patternSet.exclude(excludes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask exclude(Spec<FileTreeElement> excludeSpec) {
        patternSet.exclude(excludeSpec);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public Set<String> getIncludes() {
        return patternSet.getIncludes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask setIncludes(Iterable<String> includes) {
        patternSet.setIncludes(includes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    @Override
    public Set<String> getExcludes() {
        return patternSet.getExcludes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceTask setExcludes(Iterable<String> excludes) {
        patternSet.setExcludes(excludes);
        return this;
    }
}
