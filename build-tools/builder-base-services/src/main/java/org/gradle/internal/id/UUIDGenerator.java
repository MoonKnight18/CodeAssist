package org.gradle.internal.id;

import java.util.UUID;

public class UUIDGenerator implements IdGenerator<UUID> {
    @Override
    public UUID generateId() {
        return UUID.randomUUID();
    }
}
