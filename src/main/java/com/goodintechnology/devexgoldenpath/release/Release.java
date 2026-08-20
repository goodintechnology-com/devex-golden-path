package com.goodintechnology.devexgoldenpath.release;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class Release {

    private final UUID id;
    private final String version;
    private final String environment;
    private final Instant createdAt;
    private final Map<CheckType, CheckStatus> checks;

    public Release(String version, String environment) {
        this.id = UUID.randomUUID();
        this.version = version;
        this.environment = environment;
        this.createdAt = Instant.now();
        this.checks = new EnumMap<>(CheckType.class);
        for (CheckType type : CheckType.values()) {
            this.checks.put(type, CheckStatus.PENDING);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getEnvironment() {
        return environment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<CheckType, CheckStatus> getChecks() {
        return checks;
    }

    public void recordCheck(CheckType type, CheckStatus status) {
        checks.put(type, status);
    }
}
