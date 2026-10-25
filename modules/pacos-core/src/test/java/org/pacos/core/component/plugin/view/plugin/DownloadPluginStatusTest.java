package org.pacos.core.component.plugin.view.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadPluginStatusTest {

    @Test
    void whenFinishedThenIsFinishedReturnsTrue() {
        assertTrue(DownloadPluginStatus.FINISHED.isFinished());
    }

    @Test
    void whenNotFinishedThenIsFinishedReturnsFalse() {
        assertFalse(DownloadPluginStatus.DOWNLOADING.isFinished());
        assertFalse(DownloadPluginStatus.INSTALLING.isFinished());
        assertFalse(DownloadPluginStatus.ERROR.isFinished());
    }
}
