package org.pacos.core.component.plugin.service;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UploadedPluginInfoTest {

    @Test
    void whenEqualsCalledWithEqualObjectsThenReturnTrue() {
        PluginDTO pluginDTO = new PluginDTO();
        byte[] fileData = new byte[]{1, 2, 3};
        String fileName = "plugin.jar";

        UploadedPluginInfo info1 = new UploadedPluginInfo(pluginDTO, fileData, fileName);
        UploadedPluginInfo info2 = new UploadedPluginInfo(pluginDTO, fileData, fileName);

        assertEquals(info1, info2);
    }

    @Test
    void whenEqualsCalledWithDifferentObjectsThenReturnFalse() {
        PluginDTO pluginDTO1 = new PluginDTO();
        PluginDTO pluginDTO2 = new PluginDTO();
        byte[] fileData = new byte[]{1, 2, 3};
        String fileName1 = "plugin1.jar";
        String fileName2 = "plugin2.jar";

        UploadedPluginInfo info1 = new UploadedPluginInfo(pluginDTO1, fileData, fileName1);
        UploadedPluginInfo info2 = new UploadedPluginInfo(pluginDTO2, fileData, fileName2);

        assertNotEquals(info1, info2);
    }

    @Test
    void whenHashCodeCalledOnEqualObjectsThenReturnSameHashCode() {
        PluginDTO pluginDTO = new PluginDTO();
        byte[] fileData = new byte[]{1, 2, 3};
        String fileName = "plugin.jar";

        UploadedPluginInfo info1 = new UploadedPluginInfo(pluginDTO, fileData, fileName);
        UploadedPluginInfo info2 = new UploadedPluginInfo(pluginDTO, fileData, fileName);

        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void whenHashCodeCalledOnDifferentObjectsThenReturnDifferentHashCodes() {
        PluginDTO pluginDTO1 = new PluginDTO();
        PluginDTO pluginDTO2 = new PluginDTO();
        byte[] fileData = new byte[]{1, 2, 3};
        String fileName1 = "plugin1.jar";
        String fileName2 = "plugin2.jar";

        UploadedPluginInfo info1 = new UploadedPluginInfo(pluginDTO1, fileData, fileName1);
        UploadedPluginInfo info2 = new UploadedPluginInfo(pluginDTO2, fileData, fileName2);

        assertNotEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void whenToStringThenNoContainsFileData() {
        UploadedPluginInfo pluginDTO = new UploadedPluginInfo(new PluginDTO(), "test".getBytes(StandardCharsets.UTF_8), "filename");
        assertEquals("UploadedPluginInfo{pluginDTO=PluginDTO{name='null', removed=false, disabled=false, errMsg='null'} , fileName='filename'}", pluginDTO.toString());
    }
}
