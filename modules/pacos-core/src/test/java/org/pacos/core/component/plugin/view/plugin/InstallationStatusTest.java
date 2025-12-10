package org.pacos.core.component.plugin.view.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstallationStatusTest {

    @Test
    void whenValueOfInitializingThenReturnInitializing() {
        assertEquals(DownloadPluginStatus.DOWNLOADING, DownloadPluginStatus.valueOf("DOWNLOADING"));
    }

    @Test
    void whenValueOfInstallingThenReturnInstalling() {
        assertEquals(DownloadPluginStatus.INSTALLING, DownloadPluginStatus.valueOf("INSTALLING"));
    }

    @Test
    void whenValueOfErrorThenReturnError() {
        assertEquals(DownloadPluginStatus.ERROR, DownloadPluginStatus.valueOf("ERROR"));
    }

    @Test
    void whenValueOfFinishedThenReturnFinished() {
        assertEquals(DownloadPluginStatus.FINISHED, DownloadPluginStatus.valueOf("FINISHED"));
    }

    @Test
    void whenValuesCalledThenReturnAllStatuses() {
        DownloadPluginStatus[] statuses = DownloadPluginStatus.values();
        assertEquals(4, statuses.length);
        assertEquals(DownloadPluginStatus.DOWNLOADING, statuses[0]);
        assertEquals(DownloadPluginStatus.INSTALLING, statuses[1]);
        assertEquals(DownloadPluginStatus.ERROR, statuses[2]);
        assertEquals(DownloadPluginStatus.FINISHED, statuses[3]);
    }
}
