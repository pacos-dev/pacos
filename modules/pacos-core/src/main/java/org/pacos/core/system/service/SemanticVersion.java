package org.pacos.core.system.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares semantic versions in {@code major.minor.patch} format, including optional
 * pre-release and build metadata.
 *
 * <p>For example, version {@code 2.10.0} is newer than {@code 2.9.9}. A stable
 * release is newer than the corresponding pre-release, so {@code 3.4.1} is newer
 * than {@code 3.4.1-SNAPSHOT}.</p>
 */
public final class SemanticVersion {

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$");
    private static final BigInteger ZERO = BigInteger.ZERO;

    private final BigInteger major;
    private final BigInteger minor;
    private final BigInteger patch;
    private final List<String> preRelease;

    public SemanticVersion(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version must not be null");
        }

        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid semantic version: " + version);
        }

        this.major = parseNumber(matcher.group(1));
        this.minor = parseOptionalNumber(matcher.group(2));
        this.patch = parseOptionalNumber(matcher.group(3));
        String preReleaseValue = matcher.group(4);
        this.preRelease = preReleaseValue == null
                ? List.of()
                : List.of(preReleaseValue.split("\\."));
    }

    public boolean isTheSameMajor(SemanticVersion other) {
        return this.major.equals(other.major);
    }

    public boolean isNewestThan(SemanticVersion other) {
        int coreComparison = compareCoreVersion(other);
        if (coreComparison != 0) {
            return coreComparison > 0;
        }

        if (preRelease.isEmpty()) {
            return !other.preRelease.isEmpty();
        }
        if (other.preRelease.isEmpty()) {
            return false;
        }

        int preReleaseComparison = comparePreRelease(other);
        return preReleaseComparison > 0;
    }

    public String getMajorVersion() {
        return this.major.toString();
    }

    private int compareCoreVersion(SemanticVersion other) {
        int comparison = major.compareTo(other.major);
        if (comparison != 0) {
            return comparison;
        }

        comparison = minor.compareTo(other.minor);
        if (comparison != 0) {
            return comparison;
        }

        return patch.compareTo(other.patch);
    }

    private int comparePreRelease(SemanticVersion other) {
        int commonLength = Math.min(preRelease.size(), other.preRelease.size());
        for (int index = 0; index < commonLength; index++) {
            String current = preRelease.get(index);
            String candidate = other.preRelease.get(index);
            int comparison = comparePreReleaseIdentifier(current, candidate);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(preRelease.size(), other.preRelease.size());
    }

    private static int comparePreReleaseIdentifier(String first, String second) {
        boolean firstNumeric = isNumeric(first);
        boolean secondNumeric = isNumeric(second);

        if (firstNumeric && secondNumeric) {
            return new BigInteger(first).compareTo(new BigInteger(second));
        }
        if (firstNumeric != secondNumeric) {
            return firstNumeric ? -1 : 1;
        }
        return first.compareTo(second);
    }

    private static boolean isNumeric(String value) {
        return value.chars().allMatch(Character::isDigit);
    }

    private static BigInteger parseOptionalNumber(String value) {
        return value == null ? ZERO : parseNumber(value);
    }

    private static BigInteger parseNumber(String value) {
        return new BigInteger(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SemanticVersion that)) {
            return false;
        }
        return major.equals(that.major)
                && minor.equals(that.minor)
                && patch.equals(that.patch)
                && preRelease.equals(that.preRelease);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, preRelease);
    }
}
