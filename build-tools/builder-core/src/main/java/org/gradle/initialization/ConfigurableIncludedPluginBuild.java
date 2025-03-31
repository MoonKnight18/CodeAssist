package org.gradle.initialization;


import org.gradle.api.initialization.IncludedBuild;

/**
 * A plugin build that is to be included in the composite.
 *
 * @since 7.0
 */
//@Incubating
public interface ConfigurableIncludedPluginBuild extends IncludedBuild {

    /**
     * Sets the name of the included plugin build.
     *
     * @param name the name of the build
     * @since 7.0
     */
//    @Incubating
    void setName(String name);
}