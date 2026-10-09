package org.pacos.core.component.plugin.service;

import org.pacos.core.component.plugin.dto.PluginDTO;

public record PluginStartRequestedEvent(PluginDTO plugin) {
}
