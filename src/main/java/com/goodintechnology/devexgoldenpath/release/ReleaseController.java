package com.goodintechnology.devexgoldenpath.release;

import com.goodintechnology.devexgoldenpath.release.dto.CreateReleaseRequest;
import com.goodintechnology.devexgoldenpath.release.dto.ReadinessResponse;
import com.goodintechnology.devexgoldenpath.release.dto.ReleaseResponse;
import com.goodintechnology.devexgoldenpath.release.dto.SubmitCheckRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/releases")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @PostMapping
    public ResponseEntity<ReleaseResponse> createRelease(@Valid @RequestBody CreateReleaseRequest request) {
        Release release = releaseService.createRelease(request);
        ReleaseResponse body = ReleaseResponse.from(release);
        return ResponseEntity.created(URI.create("/releases/" + release.getId())).body(body);
    }

    @GetMapping("/{id}")
    public ReleaseResponse getRelease(@PathVariable UUID id) {
        return ReleaseResponse.from(releaseService.getRelease(id));
    }

    @PostMapping("/{id}/checks")
    public ReleaseResponse submitCheck(@PathVariable UUID id, @Valid @RequestBody SubmitCheckRequest request) {
        Release release = releaseService.submitCheck(id, request);
        return ReleaseResponse.from(release);
    }

    @GetMapping("/{id}/readiness")
    public ReadinessResponse getReadiness(@PathVariable UUID id) {
        return releaseService.computeReadiness(id);
    }
}
