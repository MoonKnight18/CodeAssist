package org.gradle.internal.resource.transport.http;

import org.gradle.api.credentials.PasswordCredentials;
import org.gradle.internal.credentials.DefaultPasswordCredentials;

public interface HttpProxySettings {

    HttpProxy getProxy();

    HttpProxy getProxy(String host);

    class HttpProxy {
        public final String host;
        public final int port;
        public final PasswordCredentials credentials;

        public HttpProxy(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            if (username == null || username.length() == 0) {
                credentials = null;
            } else {
                credentials = new DefaultPasswordCredentials(username, password);
            }
        }
    }
}
