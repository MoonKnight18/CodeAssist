package org.gradle.internal.remote.internal.hub;

import org.gradle.internal.remote.internal.hub.protocol.EndOfStream;
import org.gradle.internal.remote.internal.hub.queue.MultiChannelQueue;

import java.util.concurrent.locks.Lock;

class IncomingQueue extends MultiChannelQueue {
    IncomingQueue(Lock lock) {
        super(lock);
    }

    public void requestStop() {
        queue(new EndOfStream());
    }
}
