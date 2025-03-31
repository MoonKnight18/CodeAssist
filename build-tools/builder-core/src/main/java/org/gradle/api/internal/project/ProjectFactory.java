package org.gradle.api.internal.project;

import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.initialization.ClassLoaderScope;
import org.gradle.groovy.scripts.TextResourceScriptSource;
import org.gradle.initialization.DefaultProjectDescriptor;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.resource.TextFileResourceLoader;
import org.gradle.util.internal.NameValidator;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ProjectFactory implements IProjectFactory {

    private final Instantiator instantiator;
    private final TextFileResourceLoader textFileResourceLoader;

    public ProjectFactory(Instantiator instantiator, TextFileResourceLoader textFileResourceLoader) {
        this.instantiator = instantiator;
        this.textFileResourceLoader = textFileResourceLoader;
    }

    @Override
    public ProjectInternal createProject(GradleInternal gradle,
                                         ProjectDescriptor descriptor,
                                         ProjectState owner,
                                         @Nullable ProjectInternal parent,
                                         ClassLoaderScope selfClassLoaderScope,
                                         ClassLoaderScope baseClassLoaderScope
    ) {
        File buildFile = descriptor.getBuildFile();
        TextResourceScriptSource source = new TextResourceScriptSource(
                textFileResourceLoader.loadFile("build file", buildFile));
        DefaultProject project = instantiator.newInstance(
                DefaultProject.class,
                descriptor.getName(),
                parent,
                descriptor.getProjectDir(),
                buildFile,
                source,
                gradle,
                owner,
                gradle.getServiceRegistryFactory(),
                selfClassLoaderScope,
                baseClassLoaderScope
        );
        project.beforeEvaluate(p -> {
            NameValidator.validate(project.getName(), "project name", DefaultProjectDescriptor.INVALID_NAME_IN_INCLUDE_HINT);
        });
        gradle.getProjectRegistry().addProject(project);
        return project;
    }
}
