package com.goodintechnology.devexgoldenpath.release.dto;

import com.goodintechnology.devexgoldenpath.release.CheckStatus;
import com.goodintechnology.devexgoldenpath.release.CheckType;
import com.goodintechnology.devexgoldenpath.release.Release;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ReleaseResponse(
        UUID id,
        String version,
        String environment,
        Instant createdAt,
        Map<CheckType, CheckStatus> checks
) {
    public static ReleaseResponse from(Release release) {
        return new ReleaseResponse(
                release.getId(),
                release.getVersion(),
                release.getEnvironment(),
                release.getCreatedAt(),
                release.getChecks()
        );
    }
}
