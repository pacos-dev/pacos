package org.pacos.core.component.plugin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public PluginUpdateResult updatePlugins(PluginsToUpdate pluginToUpdate) {
        List<PluginDTO> updatedPlugins = new ArrayList<>();
        List<PluginDTO> failedPlugins = new ArrayList<>();

        for (PluginDTO requestedPlugin : pluginToUpdate.plugins()) {
            PluginDTO downloadedPlugin;
            try {
                boolean sameVersionInstalled = pluginService
                        .findByArtifactNameAndGroupId(requestedPlugin.getArtifactName(), requestedPlugin.getGroupId())
                        .stream()
                        .anyMatch(installed -> Objects.equals(installed.getVersion(), requestedPlugin.getVersion()));
                if (sameVersionInstalled) {
                    requestedPlugin.setErrMsg("The requested plugin version is already installed");
                    failedPlugins.add(requestedPlugin);
                    continue;
                }

                downloadedPlugin = PluginDownloadService.downloadPlugin(
                        pluginToUpdate.repository(), requestedPlugin.toArtifact(), requestedPlugin);
            } catch (RuntimeException exception) {
                LOG.error("Failed to download plugin {}", requestedPlugin, exception);
                failedPlugins.add(requestedPlugin);
                continue;
            }

            if (downloadedPlugin == null) {
                requestedPlugin.setErrMsg("Plugin download returned no result");
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
        } catch (RuntimeException exception) {
            restoreOldPlugins(oldPlugins, previouslyRunning, exception);
            throw exception;
        }

        try {
            pluginInstallService.savePluginForUpdate(newPlugin);
            boolean started = pluginManager.startPlugin(newPlugin).join();
            if (!started) {
                throw new IllegalStateException("Updated plugin could not be started: " + newPlugin);
            }
        } catch (RuntimeException exception) {
            try {
                pluginManager.removePlugin(newPlugin);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            try {
                pluginService.removePluginVersion(newPlugin);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            restoreOldPlugins(oldPlugins, previouslyRunning, exception);
            throw exception;
        }

        for (PluginDTO oldPlugin : oldPlugins) {
            try {
                pluginService.removePluginVersion(oldPlugin);
            } catch (RuntimeException cleanupException) {
                LOG.error("Updated plugin is running, but old version cleanup failed for {}", oldPlugin,
                        cleanupException);
            }
        }
    }

    private void restoreOldPlugins(List<PluginDTO> oldPlugins, List<PluginDTO> previouslyRunning,
                                   RuntimeException originalFailure) {
        for (PluginDTO plugin : oldPlugins) {
            try {
                if (pluginState.getState(plugin) == null) {
                    pluginManager.addPlugin(plugin);
                }
            } catch (RuntimeException restoreFailure) {
                originalFailure.addSuppressed(restoreFailure);
            }
        }

        for (PluginDTO plugin : previouslyRunning) {
            try {
                boolean restarted = pluginManager.startPlugin(plugin).join();
                if (!restarted) {
                    originalFailure.addSuppressed(
                            new IllegalStateException("Could not restore plugin after failed update: " + plugin));
                }
            } catch (RuntimeException restoreFailure) {
                originalFailure.addSuppressed(restoreFailure);
            }
        }
    }
}
