package org.pacos.core.component.plugin.loader;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;

public final class PluginInfoLoader {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledFuture;
    private static volatile long requestGeneration;

    private PluginInfoLoader() {
    }

    public static void refreshPluginList(PluginRefreshListener pluginRefreshListener) {
        refreshPluginList(AppRepository.pluginRepo(), pluginRefreshListener);
    }

    public static synchronized void refreshPluginList(AppRepository repository,
                                                       PluginRefreshListener pluginRefreshListener) {
        long generation = ++requestGeneration;
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
        Callable<List<Plugin>> loadTask = () -> refreshPluginListTask(repository, pluginRefreshListener, generation);
        scheduledFuture = executorService.schedule(loadTask, 0, TimeUnit.SECONDS);
    }

    public static synchronized void killProcess() {
        requestGeneration++;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    static List<Plugin> refreshPluginListTask(AppRepository repository, PluginRefreshListener pluginRefreshListener) {
        return refreshPluginListTask(repository, pluginRefreshListener, -1);
    }

    private static List<Plugin> refreshPluginListTask(AppRepository repository,
                                                       PluginRefreshListener pluginRefreshListener, long generation) {
        List<Plugin> list;
        try {
            list = RepositoryClient.loadPluginList(repository);
        } catch (Exception e) {
            list = null;
        }
        if (generation == -1 || generation == requestGeneration) {
            pluginRefreshListener.listLoaded(list);
        }
        return list == null ? List.of() : list;
    }
}
