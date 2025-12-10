package org.pacos.core.component.plugin.service.data;

import java.util.List;

import org.pacos.core.component.plugin.dto.PluginDTO;

/**
 * Contains plugin update result
 **/
public record PluginUpdateResult(List<PluginDTO> updated, List<PluginDTO> failed) {

    public String updateResult() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Updated plugins: %s%n", updated().size()));

        updated().forEach(plugin ->
                sb.append(String.format("\tPlugin='%s' | version='%s'%n", plugin.getName(), plugin.getVersion())));
        sb.append(String.format("Failed plugins update: %s%n", failed().size()));

        failed().forEach(plugin ->
                sb.append(String.format("\tPlugin='%s' | version='%s' | errorMsg='%s'%n", plugin.getName(), plugin.getVersion(), plugin.getErrMsg())));
        return sb.toString();
    }
}
