package org.pacos.core.component.plugin.dto;

import org.pacos.core.component.plugin.domain.AppPlugin;

public final class PacosPluginDTOMapper {

    private PacosPluginDTOMapper() {
    }

    public static PluginDTO map(AppPlugin pacosPlugin) {
        PluginDTO dto = new PluginDTO();
        dto.setVersion(pacosPlugin.getVersion());
        dto.setArtifactName(pacosPlugin.getArtifactName());
        dto.setGroupId(pacosPlugin.getGroupId());
        dto.setRemoved(pacosPlugin.isRemoved());
        dto.setDisabled(pacosPlugin.isDisabled());
        dto.setName(pacosPlugin.getName());
        dto.setIcon(pacosPlugin.getIcon());
        dto.setRepoUrl(pacosPlugin.getRepoUrl());
        dto.setAuthor(pacosPlugin.getAuthor());
        return dto;
    }
}
