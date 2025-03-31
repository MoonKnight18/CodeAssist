package org.gradle.initialization.buildsrc;

import org.gradle.api.Action;
import org.gradle.api.internal.project.ProjectInternal;

/**
 * Can be implemented by plugins to auto-configure the buildSrc root project.
 *
 * <p>Implementations are discovered using the JAR service locator mechanism (see {@link org.gradle.internal.service.ServiceLocator}).
 * Each action is invoked for the buildSrc project that is to be configured, before the project has been configured. Actions are executed
 * in an arbitrary order.
 */
public interface BuildSrcProjectConfigurationAction extends Action<ProjectInternal> {
}
