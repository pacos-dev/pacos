package org.pacos.core.component.plugin.loader;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.data.AppRepositoryArtifact;
import org.pacos.config.repository.info.PluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginDetailsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginDetailsLoader.class);
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledFuture;
    private static volatile long requestGeneration;

    private PluginDetailsLoader() {

    }

    public static void loadPluginDetails(AppArtifact artifact, PluginDetailsRefreshListener listener) {
        loadPluginDetails(artifact, AppRepository.pluginRepo(), listener);
    }

    public static synchronized void loadPluginDetails(AppArtifact artifact, AppRepository repository,
                                                       PluginDetailsRefreshListener listener) {
        long generation = ++requestGeneration;
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
        Callable<PluginInfo> loadTask = () -> loadPluginDetailTask(artifact, repository,
                details -> {
                    if (generation == requestGeneration) {
                        listener.refreshed(details);
                    }
                });
        scheduledFuture = executorService.schedule(loadTask, 0, TimeUnit.SECONDS);
    }

    public static synchronized void killProcess() {
        requestGeneration++;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    static PluginInfo loadPluginDetailTask(AppArtifact artifact, AppRepository repository, PluginDetailsRefreshListener listener) {
        PluginInfo list = null;
        try {
            list = RepositoryClient.loadInfo(new AppRepositoryArtifact(artifact, repository));
        } catch (RuntimeException ignored) {
            LOG.error("Can't load plugin details: {}", artifact);
        }
        listener.refreshed(list);
        return list;
    }

}
