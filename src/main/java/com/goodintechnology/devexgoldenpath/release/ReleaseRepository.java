package com.goodintechnology.devexgoldenpath.release;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ReleaseRepository {

    private final ConcurrentHashMap<UUID, Release> releases = new ConcurrentHashMap<>();

    public Release save(Release release) {
        releases.put(release.getId(), release);
        return release;
    }

    public Optional<Release> findById(UUID id) {
        return Optional.ofNullable(releases.get(id));
    }
}
