package com.goodintechnology.devexgoldenpath.release;

import java.util.UUID;

public class ReleaseNotFoundException extends RuntimeException {

    public ReleaseNotFoundException(UUID id) {
        super("Release not found: " + id);
    }
}
