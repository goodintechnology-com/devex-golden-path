package com.goodintechnology.devexgoldenpath.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Throwaway file for the SonarCloud exercise in issue #5 -- deliberately
 * rough code, not wired into the app or its tests. Removed once the
 * quality gate demo is done.
 */
public class QualityDemo {

    private static final String password = "SuperSecret123";

    public int classify(int value) {
        if (value > 0) {
            if (value > 10) {
                if (value > 100) {
                    return 3;
                } else {
                    return 2;
                }
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public List<String> buildLabelsA() {
        List<String> labels = new ArrayList<>();
        labels.add("small");
        labels.add("medium");
        labels.add("large");
        return labels;
    }

    public List<String> buildLabelsB() {
        List<String> labels = new ArrayList<>();
        labels.add("small");
        labels.add("medium");
        labels.add("large");
        return labels;
    }

    public void risky() {
        try {
            Integer.parseInt("not-a-number");
        } catch (NumberFormatException e) {
        }
    }
}
