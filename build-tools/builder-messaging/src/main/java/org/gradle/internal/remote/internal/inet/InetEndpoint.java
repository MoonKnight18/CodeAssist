package org.gradle.internal.remote.internal.inet;

import org.gradle.internal.remote.Address;

import java.net.InetAddress;
import java.util.List;

/**
 * A Inet-based route. Has a port and a set of potential inet addresses.
 */
public interface InetEndpoint extends Address {
    int getPort();

    List<InetAddress> getCandidates();
}
