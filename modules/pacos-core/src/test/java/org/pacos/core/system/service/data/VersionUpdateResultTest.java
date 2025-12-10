package org.pacos.core.system.service.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionUpdateResultTest {

    @Test
    void whenPreviousVersionNullThenIsToUpdateFalse() {
        SystemUpdateResult result = new SystemUpdateResult(null, "1.0");
        assertFalse(result.isToUpdate());
    }

    @Test
    void whenCurrentVersionNullThenIsToUpdateFalse() {
        SystemUpdateResult result = new SystemUpdateResult("1.0", null);
        assertFalse(result.isToUpdate());
    }

    @Test
    void whenPreviousAndCurrentVersionSameThenIsToUpdateFalse() {
        SystemUpdateResult result = new SystemUpdateResult("1.0", "1.0");
        assertFalse(result.isToUpdate());
    }

    @Test
    void whenPreviousAndCurrentVersionDifferentThenIsToUpdateTrue() {
        SystemUpdateResult result = new SystemUpdateResult("1.0", "2.0");
        assertTrue(result.isToUpdate());
    }

    @Test
    void whenIsToUpdateTrueThenUpdateResultShowsUpdatedMessage() {
        SystemUpdateResult result = new SystemUpdateResult("1.0", "2.0");
        String expectedMessage = "System update result: \n\tUpdated from version='1.0' to version='2.0'\n";
        assertEquals(expectedMessage.trim(), result.updateResult().trim());
    }

    @Test
    void whenIsToUpdateFalseThenUpdateResultShowsNoUpdatesMessage() {
        SystemUpdateResult result = new SystemUpdateResult("1.0", "1.0");
        String expectedMessage = "System update result: \n\tNo updates";
        assertEquals(expectedMessage, result.updateResult());
    }
}
