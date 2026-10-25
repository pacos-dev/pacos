package org.pacos.core.system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource({
            "1.2.3, 1.2.2, true",
            "1.2.2, 1.2.3, false",
            "1.3.0, 1.2.9, true",
            "1.2.3, 1.3.1, false",
            "2.0.0, 1.9.9, true",
            "1.2.3, 2.0.0, false"
    })
    void whenCallIsNewestThanThenReturnExpectedResult(String candidateVersion, String comparedVersion, boolean expected) {
        assertEquals(expected,
                new SemanticVersion(candidateVersion).isNewestThan(new SemanticVersion(comparedVersion)));
    }

    @Test
    void whenCallIsNewestThanAcrossTwoDigitMinorVersionsThenCompareNumerically {
        assertTrue(new SemanticVersion("3.10.0").isNewestThan(new SemanticVersion("3.9.9")));
        assertFalse(new SemanticVersion("3.9.9").isNewestThan(new SemanticVersion("3.10.0")));
    }

    @Test
    void whenCallIsNewestThanAcrossTwoDigitPatchVersionsThenCompareNumerically() {
        assertTrue(new SemanticVersion("3.4.10").isNewestThan(new SemanticVersion("3.4.9")));
    }

    @Test
    void whenStableReleaseIsComparedWithSnapshotThenStableReleaseIsNewer() {
        assertTrue(new SemanticVersion("3.4.1").isNewestThan(new SemanticVersion("3.4.1-SNAPSHOT")));
        assertFalse(new SemanticVersion("3.4.1-SNAPSHOT").isNewestThan(new SemanticVersion("3.4.1")));
    }

    @Test
    void whenCoreVersionIsEqualThenBuildMetadataDoesNotAffectPrecedence() {
        assertFalse(new SemanticVersion("3.4.1+build.1").isNewestThan(new SemanticVersion("3.4.1+build.2")));
        assertFalse(new SemanticVersion("3.4.1+build.2").isNewestThan(new SemanticVersion("3.4.1+build.1")));
    }

    @Test
    void whenVersionIsMalformedThenRejectIt() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("3.x.1"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("3.4.1-alpha..beta"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("3.4.1+build."));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(null));
    }

    @Test
    void whenVersionHasLongInvalidSuffixThenRejectIt() {
        String version = "1-" + "a".repeat(100_000) + "!";
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(version));
    }
}
