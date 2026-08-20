package com.goodintechnology.devexgoldenpath.release.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReleaseRequest(
        @NotBlank String version,
        @NotBlank String environment
) {
}
