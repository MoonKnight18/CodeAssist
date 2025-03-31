package org.gradle.composite.internal;

import org.gradle.BuildResult;
import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.internal.BuildDefinition;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.internal.artifacts.DefaultBuildIdentifier;
import org.gradle.initialization.IncludedBuildSpec;
import org.gradle.initialization.RootBuildLifecycleListener;
import org.gradle.initialization.exception.ExceptionAnalyser;
import org.gradle.initialization.layout.BuildLayout;
import org.gradle.internal.InternalBuildAdapter;
import org.gradle.internal.build.BuildLifecycleController;
import org.gradle.internal.build.BuildStateRegistry;
import org.gradle.internal.build.RootBuildState;
import org.gradle.internal.buildtree.BuildOperationFiringBuildTreeWorkExecutor;
import org.gradle.internal.buildtree.BuildTreeFinishExecutor;
import org.gradle.internal.buildtree.BuildTreeLifecycleController;
import org.gradle.internal.buildtree.BuildTreeLifecycleControllerFactory;
import org.gradle.internal.buildtree.BuildTreeState;
import org.gradle.internal.buildtree.BuildTreeWorkExecutor;
import org.gradle.internal.buildtree.DefaultBuildTreeFinishExecutor;
import org.gradle.internal.buildtree.DefaultBuildTreeWorkExecutor;
import org.gradle.internal.buildtree.ExecutionPhaseNotifyingBuildTreeWorkExecutor;
import org.gradle.internal.composite.IncludedBuildInternal;
import org.gradle.internal.enterprise.core.GradleEnterprisePluginManager;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.service.scopes.BuildScopeServices;
import org.gradle.util.Path;

import java.io.File;
import java.util.function.Function;

class DefaultRootBuildState extends AbstractCompositeParticipantBuildState implements RootBuildState {
    private final ListenerManager listenerManager;
    private final BuildTreeLifecycleController buildTreeLifecycleController;
    private boolean completed;

    DefaultRootBuildState(
            BuildDefinition buildDefinition,
            BuildTreeState buildTree,
            ListenerManager listenerManager,
            GradleEnterprisePluginManager enterprisePluginManager
    ) {
        super(buildTree, buildDefinition, null);
        this.listenerManager = listenerManager;

        BuildScopeServices buildScopeServices = getBuildServices();
        BuildLifecycleController buildLifecycleController = getBuildController();
        ExceptionAnalyser exceptionAnalyser = buildScopeServices.get(ExceptionAnalyser.class);
        BuildOperationExecutor buildOperationExecutor = buildScopeServices.get(BuildOperationExecutor.class);
        BuildStateRegistry buildStateRegistry = buildScopeServices.get(BuildStateRegistry.class);
        BuildTreeLifecycleControllerFactory buildTreeLifecycleControllerFactory = buildScopeServices.get(BuildTreeLifecycleControllerFactory.class);
        BuildTreeWorkExecutor workExecutor = new ExecutionPhaseNotifyingBuildTreeWorkExecutor(
                new BuildOperationFiringBuildTreeWorkExecutor(new DefaultBuildTreeWorkExecutor(), buildOperationExecutor),
                enterprisePluginManager);
        BuildTreeFinishExecutor finishExecutor = new DefaultBuildTreeFinishExecutor(buildStateRegistry, exceptionAnalyser, buildLifecycleController);
        this.buildTreeLifecycleController = buildTreeLifecycleControllerFactory.createRootBuildController(buildLifecycleController, workExecutor, finishExecutor);
    }

    @Override
    public BuildIdentifier getBuildIdentifier() {
        return DefaultBuildIdentifier.ROOT;
    }

    @Override
    public Path getIdentityPath() {
        return Path.ROOT;
    }

    @Override
    public boolean isImplicitBuild() {
        return false;
    }

    @Override
    public void assertCanAdd(IncludedBuildSpec includedBuildSpec) {
    }

    @Override
    public File getBuildRootDir() {
        return getBuildController().getGradle().getServices().get(BuildLayout.class).getRootDirectory();
    }

    @Override
    public IncludedBuildInternal getModel() {
        return new IncludedRootBuild(this);
    }

    @Override
    public <T> T run(Function<? super BuildTreeLifecycleController, T> action) {
        if (completed) {
            throw new IllegalStateException("Cannot run more than one action for a build.");
        }
        try {
            RootBuildLifecycleListener buildLifecycleListener = listenerManager.getBroadcaster(RootBuildLifecycleListener.class);
            buildLifecycleListener.afterStart();
            try {
                GradleInternal gradle = getBuildController().getGradle();
//                DefaultDeploymentRegistry deploymentRegistry = gradle.getServices().get(DefaultDeploymentRegistry.class);
//                gradle.addBuildListener(new InternalBuildAdapter() {
//                    @Override
//                    public void buildFinished(BuildResult result) {
//                        deploymentRegistry.buildFinished(result);
//                    }
//                });
                return action.apply(buildTreeLifecycleController);
            } finally {
                buildLifecycleListener.beforeComplete();
            }
        } finally {
            completed = true;
        }
    }

    @Override
    public StartParameterInternal getStartParameter() {
        return getBuildController().getGradle().getStartParameter();
    }

    @Override
    public Path getCurrentPrefixForProjectsInChildBuilds() {
        return Path.ROOT;
    }

    @Override
    public Path calculateIdentityPathForProject(Path path) {
        return path;
    }

    @Override
    protected void ensureChildBuildConfigured() {
        // nothing to do for the root build
    }
}