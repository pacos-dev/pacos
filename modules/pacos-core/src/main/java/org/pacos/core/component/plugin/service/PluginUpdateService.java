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
            processRequestedPlugin(pluginToUpdate, requestedPlugin, updatedPlugins, failedPlugins);
        }
        return new PluginUpdateResult(updatedPlugins, failedPlugins);
    }

    private void processRequestedPlugin(PluginsToUpdate request, PluginDTO requestedPlugin,
                                        List<PluginDTO> updatedPlugins, List<PluginDTO> failedPlugins) {
        PluginDTO downloadedPlugin = downloadPlugin(request, requestedPlugin, failedPlugins);
        if (downloadedPlugin == null || downloadedPlugin.getErrMsg() != null) {
            if (downloadedPlugin != null && !failedPlugins.contains(downloadedPlugin)) {
                failedPlugins.add(downloadedPlugin);
            }
            return;
        }

        try {
            installPlugin(downloadedPlugin);
            updatedPlugins.add(downloadedPlugin);
        } catch (RuntimeException exception) {
            LOG.error("Failed to install updated plugin {}", downloadedPlugin, exception);
            failedPlugins.add(downloadedPlugin);
        }
    }

    private PluginDTO downloadPlugin(PluginsToUpdate request, PluginDTO requestedPlugin,
                                     List<PluginDTO> failedPlugins) {
        try {
            boolean sameVersionInstalled = pluginService
                    .findByArtifactNameAndGroupId(requestedPlugin.getArtifactName(), requestedPlugin.getGroupId())
                    .stream()
                    .anyMatch(installed -> Objects.equals(installed.getVersion(), requestedPlugin.getVersion()));
            if (sameVersionInstalled) {
                requestedPlugin.setErrMsg("The requested plugin version is already installed");
                failedPlugins.add(requestedPlugin);
                return null;
            }

            PluginDTO downloaded = PluginDownloadService.downloadPlugin(
                    request.repository(), requestedPlugin.toArtifact(), requestedPlugin);
            if (downloaded == null) {
                requestedPlugin.setErrMsg("Plugin download returned no result");
                failedPlugins.add(requestedPlugin);
            } else if (downloaded.getErrMsg() != null) {
                failedPlugins.add(downloaded);
            }
            return downloaded;
        } catch (RuntimeException exception) {
            LOG.error("Failed to download plugin {}", requestedPlugin, exception);
            failedPlugins.add(requestedPlugin);
            return null;
        }
    }

    private void installPlugin(PluginDTO newPlugin) {
        List<PluginDTO> oldPlugins =
                pluginService.findByArtifactNameAndGroupId(newPlugin.getArtifactName(), newPlugin.getGroupId());
        List<PluginSnapshot> previousStates = snapshotOldPlugins(oldPlugins);

        stopAndRemoveOldPlugins(oldPlugins, previousStates);
        installAndStartNewPlugin(newPlugin, previousStates);
        removeOldVersions(oldPlugins);
    }

    private List<PluginSnapshot> snapshotOldPlugins(List<PluginDTO> oldPlugins) {
        return oldPlugins.stream()
                .map(plugin -> new PluginSnapshot(plugin, pluginState.getState(plugin)))
                .toList();
    }

    private void stopAndRemoveOldPlugins(List<PluginDTO> oldPlugins, List<PluginSnapshot> previousStates) {
        try {
            for (PluginDTO oldPlugin : oldPlugins) {
                if (!Boolean.TRUE.equals(pluginManager.stopPlugin(oldPlugin).join())) {
                    throw new IllegalStateException("Plugin could not be stopped: " + oldPlugin);
                }
            }
            for (PluginDTO oldPlugin : oldPlugins) {
                if (!pluginState.canRun(oldPlugin)) {
                    throw new IllegalStateException("Plugin is not in a removable state: " + oldPlugin);
                }
            }
            oldPlugins.forEach(pluginManager::removePlugin);
        } catch (RuntimeException exception) {
            restoreOldPlugins(previousStates, exception);
            throw exception;
        }
    }

    private void installAndStartNewPlugin(PluginDTO newPlugin, List<PluginSnapshot> previousStates) {
        try {
            try {
                pluginInstallService.savePluginForUpdate(newPlugin);
            } catch (RuntimeException persistenceFailure) {
                pluginInstallService.cleanupDownloadedArtifact(newPlugin, persistenceFailure);
                throw persistenceFailure;
            }
            if (!Boolean.TRUE.equals(pluginManager.startPlugin(newPlugin).join())) {
                throw new IllegalStateException("Updated plugin could not be started: " + newPlugin);
            }
        } catch (RuntimeException exception) {
            cleanupFailedUpdate(newPlugin, exception);
            restoreOldPlugins(previousStates, exception);
            throw exception;
        }
    }

    private void cleanupFailedUpdate(PluginDTO newPlugin, RuntimeException originalFailure) {
        try {
            pluginManager.removePlugin(newPlugin);
        } catch (RuntimeException cleanupException) {
            originalFailure.addSuppressed(cleanupException);
        }
        try {
            pluginService.removePluginVersion(newPlugin);
        } catch (RuntimeException cleanupException) {
            originalFailure.addSuppressed(cleanupException);
        }
    }

    private void removeOldVersions(List<PluginDTO> oldPlugins) {
        for (PluginDTO oldPlugin : oldPlugins) {
            try {
                pluginService.removePluginVersion(oldPlugin);
            } catch (RuntimeException cleanupException) {
                LOG.error("Updated plugin is running, but old version cleanup failed for {}", oldPlugin,
                        cleanupException);
            }
        }
    }

    private void restoreOldPlugins(List<PluginSnapshot> previousStates, RuntimeException originalFailure) {
        for (PluginSnapshot snapshot : previousStates) {
            PluginDTO plugin = snapshot.plugin();
            PluginStatusEnum previousState = snapshot.state();
            if (previousState == null) {
                continue;
            }
            try {
                if (pluginState.getState(plugin) == null) {
                    pluginManager.addPlugin(plugin);
                }
                if (previousState != PluginStatusEnum.ON && previousState != PluginStatusEnum.INITIALIZATION) {
                    pluginState.setState(plugin, previousState);
                }
            } catch (RuntimeException restoreFailure) {
                originalFailure.addSuppressed(restoreFailure);
            }
        }

        for (PluginSnapshot snapshot : previousStates) {
            PluginStatusEnum previousState = snapshot.state();
            if (previousState != PluginStatusEnum.ON && previousState != PluginStatusEnum.INITIALIZATION) {
                continue;
            }
            try {
                if (pluginState.getState(snapshot.plugin()) == null) {
                    throw new IllegalStateException(
                            "Plugin was not registered during rollback: " + snapshot.plugin());
                }
                boolean restarted = pluginManager.startPlugin(snapshot.plugin()).join();
                if (!restarted) {
                    originalFailure.addSuppressed(new IllegalStateException(
                            "Could not restore plugin after failed update: " + snapshot.plugin()));
                }
            } catch (RuntimeException restoreFailure) {
                originalFailure.addSuppressed(restoreFailure);
            }
        }
    }

    private record PluginSnapshot(PluginDTO plugin, PluginStatusEnum state) {
    }
}
