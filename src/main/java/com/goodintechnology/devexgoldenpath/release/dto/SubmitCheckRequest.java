package com.goodintechnology.devexgoldenpath.release.dto;

import com.goodintechnology.devexgoldenpath.release.CheckStatus;
import com.goodintechnology.devexgoldenpath.release.CheckType;
import jakarta.validation.constraints.NotNull;

public record SubmitCheckRequest(
        @NotNull CheckType checkType,
        @NotNull CheckStatus status
) {
}
