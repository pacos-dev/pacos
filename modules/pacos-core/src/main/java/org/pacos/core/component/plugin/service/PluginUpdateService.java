package org.pacos.core.component.plugin.service;

import java.util.ArrayList;
import java.util.List;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
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
    private final PluginState pluginState;

    @Autowired
    public PluginUpdateService(PluginInstallService pluginInstallService,
                               PluginManager pluginManager,
                               PluginService pluginService,
                               PluginState pluginState) {
        this.pluginInstallService = pluginInstallService;
        this.pluginManager = pluginManager;
        this.pluginService = pluginService;
        this.pluginState = pluginState;
    }

    @Transactional("coreTransactionManager")
    public PluginUpdateResult updatePlugins(PluginsToUpdate pluginToUpdate) {
        List<PluginDTO> updatedPlugins = new ArrayList<>();
        List<PluginDTO> failedPlugins = new ArrayList<>();

        for (PluginDTO requestedPlugin : pluginToUpdate.plugins()) {
            PluginDTO downloadedPlugin;
            try {
                downloadedPlugin = PluginDownloadService.downloadPlugin(
                        pluginToUpdate.repository(), requestedPlugin.toArtifact(), requestedPlugin);
            } catch (RuntimeException exception) {
                LOG.error("Failed to download plugin {}", requestedPlugin, exception);
                failedPlugins.add(requestedPlugin);
                continue;
            }

            if (downloadedPlugin.getErrMsg() != null) {
                failedPlugins.add(downloadedPlugin);
                continue;
            }

            try {
                installPlugin(downloadedPlugin);
                updatedPlugins.add(downloadedPlugin);
            } catch (RuntimeException exception) {
                LOG.error("Failed to install updated plugin {}", downloadedPlugin, exception);
                failedPlugins.add(downloadedPlugin);
            }
        }

        return new PluginUpdateResult(updatedPlugins, failedPlugins);
    }

    private void installPlugin(PluginDTO newPlugin) {
        List<PluginDTO> oldPlugins =
                pluginService.findByArtifactNameAndGroupId(newPlugin.getArtifactName(), newPlugin.getGroupId());
        List<PluginDTO> previouslyRunning = new ArrayList<>();

        try {
            for (PluginDTO oldPlugin : oldPlugins) {
                PluginStatusEnum previousState = pluginState.getState(oldPlugin);
                if (previousState == PluginStatusEnum.ON || previousState == PluginStatusEnum.INITIALIZATION) {
                    previouslyRunning.add(oldPlugin);
                }

                boolean stopped = pluginManager.stopPlugin(oldPlugin).join();
                if (!stopped) {
                    throw new IllegalStateException("Plugin could not be stopped: " + oldPlugin);
                }
            }

            for (PluginDTO oldPlugin : oldPlugins) {
                if (!pluginState.canRun(oldPlugin)) {
                    throw new IllegalStateException("Plugin is not in a removable state: " + oldPlugin);
                }
            }

            for (PluginDTO oldPlugin : oldPlugins) {
                pluginManager.removePlugin(oldPlugin);
            }

            if (!oldPlugins.isEmpty()) {
                pluginService.removePlugin(oldPlugins.get(0));
            }
        } catch (RuntimeException exception) {
            restartPreviouslyRunning(previouslyRunning, exception);
            throw exception;
        }

        pluginInstallService.savePlugin(newPlugin);
        boolean started = pluginManager.startPlugin(newPlugin).join();
        if (!started) {
            throw new IllegalStateException("Updated plugin could not be started: " + newPlugin);
        }
    }

    private void restartPreviouslyRunning(List<PluginDTO> previouslyRunning, RuntimeException originalFailure) {
        for (PluginDTO plugin : previouslyRunning) {
            try {
                if (pluginState.getState(plugin) == null) {
                    pluginManager.addPlugin(plugin);
                }
                boolean restarted = pluginManager.startPlugin(plugin).join();
                if (!restarted && originalFailure != null) {
                    originalFailure.addSuppressed(
                            new IllegalStateException("Could not restore plugin after failed update: " + plugin));
                }
            } catch (RuntimeException restartFailure) {
                if (originalFailure != null) {
                    originalFailure.addSuppressed(restartFailure);
                } else {
                    LOG.error("Could not restore plugin after failed update: {}", plugin, restartFailure);
                }
            }
        }
    }
}
