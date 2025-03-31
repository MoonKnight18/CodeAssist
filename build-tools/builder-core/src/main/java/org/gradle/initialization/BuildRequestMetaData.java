package org.gradle.initialization;

import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;

/**
 * A bunch of information about the request which launched a build.
 */
@ServiceScope(Scopes.BuildSession.class)
public interface BuildRequestMetaData {

    /**
     * Returns the meta-data about the client used to launch this build.
     */
    BuildClientMetaData getClient();

    /**
     * The time that the request was made by the user of the client.
     */
    long getStartTime();

    boolean isInteractive();
}