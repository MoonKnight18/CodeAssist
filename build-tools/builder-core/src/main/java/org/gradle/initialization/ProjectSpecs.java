package org.gradle.initialization;

import org.gradle.StartParameter;
import org.gradle.api.internal.SettingsInternal;

import java.io.File;

class ProjectSpecs {

    static ProjectSpec forStartParameter(StartParameter startParameter, SettingsInternal settings) {
        File explicitProjectDir = startParameter.getProjectDir();
        @SuppressWarnings("deprecation")
        File explicitBuildFile = startParameter.getBuildFile();
        if (explicitBuildFile != null) {
            return new BuildFileProjectSpec(explicitBuildFile);
        }
        if (explicitProjectDir != null) {
            return new ProjectDirectoryProjectSpec(explicitProjectDir);
        }
        return new CurrentDirectoryProjectSpec(startParameter.getCurrentDir(), settings);
    }
}
