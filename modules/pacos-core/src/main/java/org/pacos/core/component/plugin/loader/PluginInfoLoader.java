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

    private PluginInfoLoader() {
    }

    public static void refreshPluginList(PluginRefreshListener pluginRefreshListener) {
        if (scheduledFuture == null || scheduledFuture.isDone() || scheduledFuture.isCancelled()) {
            AppRepository repository = AppRepository.pluginRepo();
            Callable<List<Plugin>> loadTask = () -> refreshPluginListTask(repository, pluginRefreshListener);
            scheduledFuture = executorService.schedule(loadTask, 0, TimeUnit.SECONDS);
        }
    }

    public static void killProcess() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    static List<Plugin> refreshPluginListTask(AppRepository repository, PluginRefreshListener pluginRefreshListener) {
        try {
            List<Plugin> list = RepositoryClient.loadPluginList(repository);
            pluginRefreshListener.listLoaded(list);
            return list;
        } catch (Exception e) {
            pluginRefreshListener.listLoaded(null);
            return List.of();
        }
    }
}
