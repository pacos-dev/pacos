package org.pacos.core.system.service;

import java.util.Objects;

/**
 * Example for version 1.2.3
 * Minor version: 1 - big changes, no backward compatibility
 * Major version: 2 - new functionality, backward compatibility
 * Path version: 3 - fixes, backward compatibility
 */
public class SemanticVersion {

    private final String major;
    private final String minor;
    private final String patch;

    public SemanticVersion(String version) {
        String[] versionArr = version.split("\\.");
        this.major = versionArr[0];
        if (versionArr.length > 1) {
            this.minor = versionArr[1];
        } else {
            this.minor = null;
        }
        if (versionArr.length > 2) {
            this.patch = versionArr[2];
        } else {
            this.patch = null;
        }
    }

    public boolean isTheSameMajor(SemanticVersion other) {
        return this.major.equalsIgnoreCase(other.major);
    }

    public boolean isNewestThan(SemanticVersion other) {
        int majorComparison = this.major.compareTo(other.major);
        if (majorComparison != 0) {
            return majorComparison > 0;
        }

        if (this.minor != null && other.minor != null) {
            int minorComparison = this.minor.compareTo(other.minor);
            if (minorComparison != 0) {
                return minorComparison > 0;
            }
        }

        if (this.patch != null && other.patch != null) {
            return this.patch.compareTo(other.patch) > 0;
        }

        return false;
    }

    public String getMajorVersion() {
        return this.major;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SemanticVersion that = (SemanticVersion) o;
        return Objects.equals(major, that.major) && Objects.equals(minor, that.minor) && Objects.equals(patch, that.patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
