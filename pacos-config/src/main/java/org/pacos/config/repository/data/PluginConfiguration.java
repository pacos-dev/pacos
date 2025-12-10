package org.pacos.config.repository.data;

import java.util.List;

import org.pacos.config.repository.info.Plugin;

/**
 * Represents plugin.json configuration fetching from remote server
 *
 * @param plugins      - list of mandatory artifacts
 */
public record PluginConfiguration(List<Plugin> plugins) {

}
