package org.pacos.core.component.plugin.dto;

import org.junit.jupiter.api.Test;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.info.Plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDTOTest {

    @Test
    void whenIsRemovedThenReturnsCorrectValue() {
        PluginDTO pluginDTO = new PluginDTO();
        assertFalse(pluginDTO.isRemoved());
        pluginDTO.setRemoved(true);
        assertTrue(pluginDTO.isRemoved());
    }

    @Test
    void whenIsDisabledThenReturnsCorrectValue() {
        PluginDTO pluginDTO = new PluginDTO();
        assertFalse(pluginDTO.isDisabled());
        pluginDTO.setDisabled(true);
        assertTrue(pluginDTO.isDisabled());
    }

    @Test
    void whenToStringThenReturnsCorrectFormat() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setName("Example Plugin");
        pluginDTO.setRemoved(true);
        pluginDTO.setDisabled(false);
        pluginDTO.setErrMsg("No errors");

        String expected = "PluginDTO{name='Example Plugin', removed=true, disabled=false, errMsg='No errors'} ";
        assertEquals(expected, pluginDTO.toString());
    }

    @Test
    void whenToArtifactThenReturnsCorrectAppArtifact() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setGroupId("com.example");
        pluginDTO.setArtifactName("artifact-name");
        pluginDTO.setVersion("1.0.0");

        AppArtifact artifact = pluginDTO.toArtifact();
        assertEquals("com.example", artifact.groupId());
        assertEquals("artifact-name", artifact.artifactName());
        assertEquals("1.0.0", artifact.version());
    }

    @Test
    void whenConstructedWithAppArtifactThenFieldsAreSetCorrectly() {
        Plugin artifact = new Plugin("com.example", "artifact-name", "", "1.0.0", "", "");
        PluginDTO pluginDTO = new PluginDTO(artifact);

        assertEquals("com.example", pluginDTO.getGroupId());
        assertEquals("artifact-name", pluginDTO.getArtifactName());
        assertEquals("1.0.0", pluginDTO.getVersion());
    }


    @Test
    void whenSetPluginDTOToNullThenReturnNull() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setPluginInfoDTO(null);

        assertNull(pluginDTO.getPluginInfoDTO());
    }

    @Test
    void whenObjectsAreSameThenEqualsReturnsTrue() {
        PluginDTO plugin = new PluginDTO();
        plugin.setGroupId("org.example");
        plugin.setArtifactName("plugin-artifact");
        plugin.setVersion("1.0");

        assertEquals(plugin, plugin);
    }

    @Test
    void whenObjectsHaveSameValuesThenEqualsReturnsTrue() {
        PluginDTO plugin1 = new PluginDTO();
        plugin1.setGroupId("org.example");
        plugin1.setArtifactName("plugin-artifact");
        plugin1.setVersion("1.0");

        PluginDTO plugin2 = new PluginDTO();
        plugin2.setGroupId("org.example");
        plugin2.setArtifactName("plugin-artifact");
        plugin2.setVersion("1.0");

        assertEquals(plugin1, plugin2);
    }

    @Test
    void whenObjectsHaveDifferentValuesThenEqualsReturnsFalse() {
        PluginDTO plugin1 = new PluginDTO();
        plugin1.setGroupId("org.example");
        plugin1.setArtifactName("plugin-artifact");
        plugin1.setVersion("1.0");

        PluginDTO plugin2 = new PluginDTO();
        plugin2.setGroupId("org.example");
        plugin2.setArtifactName("different-artifact");
        plugin2.setVersion("1.0");

        assertNotEquals(plugin1, plugin2);
    }

    @Test
    void whenObjectsHaveSameValuesThenHashCodeIsSame() {
        PluginDTO plugin1 = new PluginDTO();
        plugin1.setGroupId("org.example");
        plugin1.setArtifactName("plugin-artifact");
        plugin1.setVersion("1.0");

        PluginDTO plugin2 = new PluginDTO();
        plugin2.setGroupId("org.example");
        plugin2.setArtifactName("plugin-artifact");
        plugin2.setVersion("1.0");

        assertEquals(plugin1.hashCode(), plugin2.hashCode());
    }

    @Test
    void whenObjectsHaveDifferentValuesThenHashCodeIsDifferent() {
        PluginDTO plugin1 = new PluginDTO();
        plugin1.setGroupId("org.example");
        plugin1.setArtifactName("plugin-artifact");
        plugin1.setVersion("1.0");

        PluginDTO plugin2 = new PluginDTO();
        plugin2.setGroupId("org.example");
        plugin2.setArtifactName("different-artifact");
        plugin2.setVersion("1.0");

        assertNotEquals(plugin1.hashCode(), plugin2.hashCode());
    }

    @Test
    void whenEqualsWithoutVersionCalledThenComparesWithoutVersion() {
        PluginDTO plugin1 = new PluginDTO();
        plugin1.setGroupId("org.example");
        plugin1.setArtifactName("plugin-artifact");
        plugin1.setVersion("1.0");

        PluginDTO plugin2 = new PluginDTO();
        plugin2.setGroupId("org.example");
        plugin2.setArtifactName("plugin-artifact");
        plugin2.setVersion("2.0");

        assertTrue(plugin1.equalsWithoutVersion(plugin2));
    }
}
