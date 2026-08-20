package com.goodintechnology.devexgoldenpath.release.dto;

import com.goodintechnology.devexgoldenpath.release.ReadinessStatus;
import java.util.List;
import java.util.UUID;

public record ReadinessResponse(
        UUID releaseId,
        String version,
        ReadinessStatus status,
        List<String> reasons
) {
}
