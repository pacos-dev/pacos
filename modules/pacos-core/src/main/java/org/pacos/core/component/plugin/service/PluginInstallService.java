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
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;
import org.pacos.core.component.session.service.ServiceListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PluginInstallService {
    private final PacosPluginRepository pluginRepository;
    private final PluginService pluginService;
    private final Map<PluginKey, DownloadPluginStatus> downloadStatus = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;
    private final PluginState pluginState;
    private final PluginFileStorageService pluginFileStorageService;

    @Autowired
    public PluginInstallService(PacosPluginRepository pluginRepository, PluginService pluginService, PluginState pluginState, PluginFileStorageService pluginFileStorageService, ApplicationEventPublisher eventPublisher) {
        this.pluginRepository = pluginRepository;
        this.pluginService = pluginService;
        this.eventPublisher = eventPublisher;
        this.pluginState = pluginState;
        this.pluginFileStorageService = pluginFileStorageService;
    }

    @PostConstruct
    public void init() {
        pluginService.findNotRemovedPlugin().forEach(
                plugin -> downloadStatus.put(PluginKey.from(plugin), DownloadPluginStatus.FINISHED));
    }

    @Transactional("coreTransactionManager")
    public void savePlugin(PluginDTO plugin) {
        removeOldPluginIfNecessary(plugin);
        persistPlugin(plugin);
    }

    @Transactional("coreTransactionManager")
    public void savePluginForUpdate(PluginDTO plugin) {
        persistPlugin(plugin);
    }

    private void persistPlugin(PluginDTO plugin) {
        AppPlugin pacosPlugin = new AppPlugin(plugin.getGroupId(),
                plugin.getArtifactName(), plugin.getVersion());
        pacosPlugin.setAuthor(plugin.getAuthor());
        pacosPlugin.setIcon(plugin.getIcon());
        pacosPlugin.setName(plugin.getName());
        pacosPlugin.setRepoUrl(plugin.getRepoUrl());
        pluginRepository.save(pacosPlugin);

        PluginIconExtractor.extractIcon(plugin);
        pluginState.addPlugin(plugin);
    }

    @Async("pluginDownloadExecutor")
    @Transactional("coreTransactionManager")
    public void downloadAndInstallPluginFromRemote(PluginDTO plugin, AppRepository appRepository) {
        PluginKey key = PluginKey.from(plugin);
        while (true) {
            DownloadPluginStatus current = downloadStatus.get(key);
            if (current == DownloadPluginStatus.FINISHED && !pluginState.getPlugins().contains(plugin)) {
                downloadStatus.remove(key, DownloadPluginStatus.FINISHED);
                continue;
            }
            if (current != null) {
                notifyDownloadState(plugin, current);
                return;
            }
            if (downloadStatus.putIfAbsent(key, DownloadPluginStatus.DOWNLOADING) == null) {
                break;
            }
        }
        try {
            notifyDownloadState(plugin, DownloadPluginStatus.DOWNLOADING);
            AppArtifact artifact = new AppArtifact(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
            PluginDownloadService.downloadPlugin(appRepository, artifact, plugin);

            if (plugin.getErrMsg() == null) {
                savePlugin(plugin);
                downloadStatus.put(key, DownloadPluginStatus.FINISHED);
                notifyDownloadState(plugin, DownloadPluginStatus.INSTALLING);
                eventPublisher.publishEvent(new PluginStartRequestedEvent(plugin));
            } else {
                downloadStatus.remove(key);
                notifyDownloadState(plugin, DownloadPluginStatus.ERROR);
            }
        } catch (RuntimeException e) {
            downloadStatus.remove(key);
            notifyDownloadState(plugin, DownloadPluginStatus.ERROR);
            throw e;
        }
    }


    public boolean isInstallationInProgress() {
        return downloadStatus.containsValue(DownloadPluginStatus.DOWNLOADING)
                || downloadStatus.containsValue(DownloadPluginStatus.INSTALLING);
    }

    private record PluginKey(String groupId, String artifactName, String version) {
        private static PluginKey from(PluginDTO plugin) {
            return new PluginKey(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
        }
    }

    private void notifyDownloadState(PluginDTO plugin, DownloadPluginStatus status) {
        ServiceListener.notifyAll(ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, new PluginDownloadState(plugin, status));
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
