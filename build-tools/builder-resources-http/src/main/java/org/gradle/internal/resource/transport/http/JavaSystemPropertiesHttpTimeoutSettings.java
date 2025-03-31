package org.gradle.internal.resource.transport.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaSystemPropertiesHttpTimeoutSettings implements HttpTimeoutSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaSystemPropertiesHttpTimeoutSettings.class);
    public static final String CONNECTION_TIMEOUT_SYSTEM_PROPERTY = "org.gradle.internal.http.connectionTimeout";
    public static final String SOCKET_TIMEOUT_SYSTEM_PROPERTY = "org.gradle.internal.http.socketTimeout";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private final int connectionTimeoutMs;
    private final int socketTimeoutMs;

    public JavaSystemPropertiesHttpTimeoutSettings() {
        this.connectionTimeoutMs = initTimeout(CONNECTION_TIMEOUT_SYSTEM_PROPERTY, DEFAULT_CONNECTION_TIMEOUT);
        this.socketTimeoutMs = initTimeout(SOCKET_TIMEOUT_SYSTEM_PROPERTY, DEFAULT_SOCKET_TIMEOUT);
    }

    @Override
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    @Override
    public int getSocketTimeoutMs() {
        return socketTimeoutMs;
    }

    private int initTimeout(String propertyName, int defaultValue) {
        String systemProperty = System.getProperty(propertyName);

        if (!StringUtils.isBlank(systemProperty)) {
            try {
                return Integer.parseInt(systemProperty);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid value for java system property '{}': {}. Default timeout '{}' will be used.",
                    propertyName, systemProperty, defaultValue);
            }
        }

        return defaultValue;
    }
}
