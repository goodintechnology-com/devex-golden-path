package com.goodintechnology.devexgoldenpath.release;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goodintechnology.devexgoldenpath.release.dto.CreateReleaseRequest;
import com.goodintechnology.devexgoldenpath.release.dto.ReadinessResponse;
import com.goodintechnology.devexgoldenpath.release.dto.SubmitCheckRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReleaseServiceTest {

    private ReleaseService releaseService;

    @BeforeEach
    void setUp() {
        releaseService = new ReleaseService(new ReleaseRepository());
    }

    private UUID createRelease() {
        Release release = releaseService.createRelease(new CreateReleaseRequest("1.8.4", "production"));
        return release.getId();
    }

    private void passAllChecksExcept(UUID id, CheckType excluded) {
        for (CheckType type : CheckType.values()) {
            if (type != excluded) {
                releaseService.submitCheck(id, new SubmitCheckRequest(type, CheckStatus.PASS));
            }
        }
    }

    @Test
    void readinessIsBlockedWhenNoChecksHaveBeenSubmitted() {
        UUID id = createRelease();

        ReadinessResponse readiness = releaseService.computeReadiness(id);

        assertThat(readiness.status()).isEqualTo(ReadinessStatus.BLOCKED);
        assertThat(readiness.reasons()).hasSize(CheckType.values().length);
    }

    @Test
    void readinessIsReadyWhenAllRequiredChecksPass() {
        UUID id = createRelease();
        for (CheckType type : CheckType.values()) {
            releaseService.submitCheck(id, new SubmitCheckRequest(type, CheckStatus.PASS));
        }

        ReadinessResponse readiness = releaseService.computeReadiness(id);

        assertThat(readiness.status()).isEqualTo(ReadinessStatus.READY);
        assertThat(readiness.reasons()).isEmpty();
    }

    @Test
    void readinessIsBlockedWhenApprovalIsMissing() {
        UUID id = createRelease();
        passAllChecksExcept(id, CheckType.APPROVAL);

        ReadinessResponse readiness = releaseService.computeReadiness(id);

        assertThat(readiness.status()).isEqualTo(ReadinessStatus.BLOCKED);
        assertThat(readiness.reasons()).containsExactly("Approval is required but has not been submitted");
    }

    @Test
    void readinessIsBlockedWhenACheckFails() {
        UUID id = createRelease();
        passAllChecksExcept(id, CheckType.SECURITY_SCAN);
        releaseService.submitCheck(id, new SubmitCheckRequest(CheckType.SECURITY_SCAN, CheckStatus.FAIL));

        ReadinessResponse readiness = releaseService.computeReadiness(id);

        assertThat(readiness.status()).isEqualTo(ReadinessStatus.BLOCKED);
        assertThat(readiness.reasons()).containsExactly("Security scan did not pass");
    }

    @Test
    void unknownReleaseIdThrows() {
        UUID randomId = UUID.randomUUID();

        assertThatThrownBy(() -> releaseService.getRelease(randomId))
                .isInstanceOf(ReleaseNotFoundException.class);
    }
}
