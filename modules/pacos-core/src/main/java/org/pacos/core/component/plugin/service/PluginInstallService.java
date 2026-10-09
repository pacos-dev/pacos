package org.pacos.core.component.plugin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sisu.PostConstruct;
import org.pacos.base.event.ModuleEvent;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;
import org.pacos.core.component.session.service.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PluginInstallService {
    private static final Logger LOG = LoggerFactory.getLogger(PluginInstallService.class);
    private final PacosPluginRepository pluginRepository;
    private final PluginService pluginService;
    private final Map<PluginDTO, DownloadPluginStatus> downloadStatus = new ConcurrentHashMap<>();
    private final PluginManager pluginManager;
    private final PluginState pluginState;
    private final PluginFileStorageService pluginFileStorageService;

    @Autowired
    public PluginInstallService(PacosPluginRepository pluginRepository, PluginService pluginService, PluginManager pluginManager, PluginState pluginState, PluginFileStorageService pluginFileStorageService) {
        this.pluginRepository = pluginRepository;
        this.pluginService = pluginService;
        this.pluginManager = pluginManager;
        this.pluginState = pluginState;
        this.pluginFileStorageService = pluginFileStorageService;
    }

    @PostConstruct
    public void init() {
        pluginService.findNotRemovedPlugin().forEach(
                plugin -> downloadStatus.put(plugin, DownloadPluginStatus.FINISHED));
    }

    @Transactional("coreTransactionManager")
    public void savePlugin(PluginDTO plugin) {
        removeOldPluginIfNecessary(plugin);

        AppPlugin pacosPlugin = new AppPlugin(plugin.getGroupId(),
                plugin.getArtifactName(), plugin.getVersion());
        pacosPlugin.setAuthor(plugin.getAuthor());
        pacosPlugin.setIcon(plugin.getIcon());
        pacosPlugin.setName(plugin.getName());
        pacosPlugin.setRepoUrl(plugin.getRepoUrl());
        pluginRepository.save(pacosPlugin);

        PluginIconExtractor.extractIcon(plugin);
        pluginManager.addPlugin(plugin);
    }

    @Async("pluginDownloadExecutor")
    @Transactional("coreTransactionManager")
    public synchronized void downloadAndInstallPluginFromRemote(PluginDTO plugin, AppRepository appRepository) {
        if (downloadStatus.containsKey(plugin) && downloadStatus.get(plugin).equals(DownloadPluginStatus.FINISHED) &&
                !pluginState.getPlugins().contains(plugin)) {
            downloadStatus.remove(plugin);
        }
        if (downloadStatus.containsKey(plugin)) {
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(plugin,
                    downloadStatus.get(plugin)));
            return;
        }
        downloadStatus.put(plugin, DownloadPluginStatus.DOWNLOADING);
        ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(plugin,
                DownloadPluginStatus.DOWNLOADING));

        AppArtifact artifact = new AppArtifact(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
        PluginDownloadService.downloadPlugin(appRepository, artifact, plugin);

        if (plugin.getErrMsg() == null) {
            savePlugin(plugin);
            downloadStatus.put(plugin, DownloadPluginStatus.FINISHED);
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(plugin,
                    DownloadPluginStatus.INSTALLING));
            pluginManager.startPlugin(plugin);
        } else {
            downloadStatus.remove(plugin);
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(plugin,
                    DownloadPluginStatus.ERROR));
        }
    }


    public boolean isInstallationInProgress() {
        return downloadStatus.containsValue(DownloadPluginStatus.DOWNLOADING) || downloadStatus.containsValue(DownloadPluginStatus.INSTALLING);
    }

    public void storePluginFile(UploadedPluginInfo pluginInfo) throws IOException {
        pluginFileStorageService.storePluginFile(pluginInfo);
    }

    private void removeOldPluginIfNecessary(PluginDTO plugin) {
        List<AppPlugin> oldPlugins = pluginRepository.
                findByArtifactNameAndGroupId(plugin.getArtifactName(), plugin.getGroupId());
        oldPlugins.forEach(pluginService::removePlugin);
    }

}
