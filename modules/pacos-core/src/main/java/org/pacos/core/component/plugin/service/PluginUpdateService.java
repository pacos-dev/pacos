package org.pacos.core.component.plugin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.service.data.PluginUpdateResult;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PluginUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginUpdateService.class);
    private final PluginInstallService pluginInstallService;
    private final PluginManager pluginManager;
    private final PluginService pluginService;

    @Autowired
    public PluginUpdateService(PluginInstallService pluginInstallService,
                               PluginManager pluginManager,
                               PluginService pluginService) {
        this.pluginInstallService = pluginInstallService;
        this.pluginManager = pluginManager;
        this.pluginService = pluginService;
    }

    @Transactional("coreTransactionManager")
    public PluginUpdateResult updatePlugins(PluginsToUpdate pluginToUpdate) {
        List<PluginDTO> updatedPlugins = new ArrayList<>();
        List<PluginDTO> failedPlugins = new ArrayList<>();
        pluginToUpdate.plugins().forEach(pluginToInstall -> {
            PluginDTO newPlugin = PluginDownloadService.downloadPlugin(pluginToUpdate.repository(), pluginToInstall.toArtifact(), pluginToInstall);
            if (newPlugin.getErrMsg() == null) {
                updatedPlugins.add(newPlugin);
                installPlugin(newPlugin, failedPlugins);
            } else {
                failedPlugins.add(newPlugin);
            }
        });
        return new PluginUpdateResult(updatedPlugins, failedPlugins);
    }

    private void installPlugin(PluginDTO newPlugin, List<PluginDTO> failedPlugins) {
        pluginService.findByArtifactNameAndGroupId(newPlugin.getArtifactName(), newPlugin.getGroupId()).forEach(oldPlugin -> {
            CompletableFuture<Boolean> completableFuture = pluginManager.stopPlugin(oldPlugin);
            completableFuture.thenAccept(result -> {
                if (Boolean.TRUE.equals(result) && PluginState.canRun(oldPlugin)) {
                    pluginService.removePlugin(oldPlugin);
                    pluginManager.removePlugin(oldPlugin);
                }
            }).thenAccept(result -> {
                pluginInstallService.savePlugin(newPlugin);
                pluginManager.startPlugin(newPlugin);
            }).exceptionallyCompose(exception -> {
                LOG.error(exception.getMessage(), exception);
                failedPlugins.add(oldPlugin);
                return null;
            });
        });
    }
}
