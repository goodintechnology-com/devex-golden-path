package com.goodintechnology.devexgoldenpath.release;

import com.goodintechnology.devexgoldenpath.release.dto.CreateReleaseRequest;
import com.goodintechnology.devexgoldenpath.release.dto.ReadinessResponse;
import com.goodintechnology.devexgoldenpath.release.dto.SubmitCheckRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ReleaseService {

    private static final Map<CheckType, String> CHECK_LABELS = Map.of(
            CheckType.UNIT_TESTS, "Unit tests",
            CheckType.QUALITY_GATE, "Quality gate",
            CheckType.SECURITY_SCAN, "Security scan",
            CheckType.SBOM, "SBOM",
            CheckType.APPROVAL, "Approval"
    );

    private final ReleaseRepository releaseRepository;

    public ReleaseService(ReleaseRepository releaseRepository) {
        this.releaseRepository = releaseRepository;
    }

    public Release createRelease(CreateReleaseRequest request) {
        Release release = new Release(request.version(), request.environment());
        return releaseRepository.save(release);
    }

    public Release getRelease(UUID id) {
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ReleaseNotFoundException(id));
    }

    public Release submitCheck(UUID id, SubmitCheckRequest request) {
        Release release = getRelease(id);
        release.recordCheck(request.checkType(), request.status());
        return release;
    }

    public ReadinessResponse computeReadiness(UUID id) {
        Release release = getRelease(id);
        List<String> reasons = new ArrayList<>();

        for (Map.Entry<CheckType, CheckStatus> entry : release.getChecks().entrySet()) {
            String label = CHECK_LABELS.get(entry.getKey());
            switch (entry.getValue()) {
                case PENDING -> reasons.add(label + " is required but has not been submitted");
                case FAIL -> reasons.add(label + " did not pass");
                case PASS -> {
                    // satisfied, no reason to report
                }
            }
        }

        ReadinessStatus status = reasons.isEmpty() ? ReadinessStatus.READY : ReadinessStatus.BLOCKED;
        return new ReadinessResponse(release.getId(), release.getVersion(), status, reasons);
    }
}
