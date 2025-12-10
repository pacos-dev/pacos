package org.pacos.core.component.plugin.manager.data;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class PluginStatusTest {

    @Test
    public void whenCreatePluginStatusThenExpectedResult() {
        PluginDTO pluginDTO = new PluginDTO();
        PluginStatusEnum statusEnum = PluginStatusEnum.ON;

        PluginStatus pluginStatus = new PluginStatus(pluginDTO, statusEnum);

        assertEquals(pluginDTO, pluginStatus.pluginDTO());
        assertEquals(statusEnum, pluginStatus.pluginState());
    }

    @Test
    public void whenCompareEqualObjectsThenExpectedResult() {
        PluginDTO pluginDTO = new PluginDTO();

        PluginStatus status1 = new PluginStatus(pluginDTO, PluginStatusEnum.ON);
        PluginStatus status2 = new PluginStatus(pluginDTO, PluginStatusEnum.ON);

        assertEquals(status1, status2);
        assertEquals(status1.hashCode(), status2.hashCode());
    }

    @Test
    public void whenCompareDifferentObjectsThenExpectedResult() {
        PluginDTO pluginDTO1 = new PluginDTO();
        pluginDTO1.setArtifactName("artifactName");
        PluginDTO pluginDTO2 = new PluginDTO();
        pluginDTO2.setArtifactName("artifactName2");

        PluginStatus status1 = new PluginStatus(pluginDTO1, PluginStatusEnum.ON);
        PluginStatus status2 = new PluginStatus(pluginDTO2, PluginStatusEnum.ON);
        PluginStatus status3 = new PluginStatus(pluginDTO1, PluginStatusEnum.OFF);

        assertNotEquals(status1, status2);
        assertNotEquals(status1, status3);
    }
}
