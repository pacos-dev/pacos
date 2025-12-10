package org.pacos.core.system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticVersionTest {

    @Test
    void whenCallIsTheSameMajorWithSameMajorThenTrue() {
        SemanticVersion version1 = new SemanticVersion("1.2.3");
        SemanticVersion version2 = new SemanticVersion("1.3.4");
        assertTrue(version1.isTheSameMajor(version2));
    }

    @Test
    void whenCallIsTheSameMajorWithDifferentMajorThenFalse() {
        SemanticVersion version1 = new SemanticVersion("1.2.3");
        SemanticVersion version2 = new SemanticVersion("2.2.3");
        assertFalse(version1.isTheSameMajor(version2));
    }

    @Test
    void whenCallIsNewestThanWithNewerPatchVersionThenTrue() {
        SemanticVersion version1 = new SemanticVersion("1.2.3");
        SemanticVersion version2 = new SemanticVersion("1.2.2");
        assertTrue(version1.isNewestThan(version2));
    }

    @Test
    void whenCallIsNewestThanWithOlderPatchVersionThenFalse() {
        SemanticVersion version1 = new SemanticVersion("1.2.2");
        SemanticVersion version2 = new SemanticVersion("1.2.3");
        assertFalse(version1.isNewestThan(version2));
    }

    @Test
    void whenCallIsNewestThanWithNewerMinorVersionThenTrue() {
        SemanticVersion version1 = new SemanticVersion("1.3.0");
        SemanticVersion version2 = new SemanticVersion("1.2.9");
        assertTrue(version1.isNewestThan(version2));
    }

    @Test
    void whenCallIsNewestThanWithOlderMinorVersionThenFalse() {
        SemanticVersion version1 = new SemanticVersion("1.2.3");
        SemanticVersion version2 = new SemanticVersion("1.3.1");
        assertFalse(version1.isNewestThan(version2));
    }

    @Test
    void whenCallIsNewestThanWithNewerMajorVersionThenTrue() {
        SemanticVersion version1 = new SemanticVersion("2.0.0");
        SemanticVersion version2 = new SemanticVersion("1.9.9");
        assertTrue(version1.isNewestThan(version2));
    }

    @Test
    void whenCallIsNewestThanWithOlderMajorVersionThenFalse() {
        SemanticVersion version1 = new SemanticVersion("1.2.3");
        SemanticVersion version2 = new SemanticVersion("2.0.0");
        assertFalse(version1.isNewestThan(version2));
    }
}

