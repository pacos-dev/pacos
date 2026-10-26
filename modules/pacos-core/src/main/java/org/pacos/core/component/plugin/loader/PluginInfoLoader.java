package org.pacos.core.component.plugin.loader;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;

public final class PluginInfoLoader {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static final Map<Object, LoadRequest> scheduledRequests = new IdentityHashMap<>();

    private PluginInfoLoader() {
    }

    public static void refreshPluginList(PluginRefreshListener pluginRefreshListener) {
        refreshPluginList(AppRepository.pluginRepo(), pluginRefreshListener, pluginRefreshListener);
    }

    public static void refreshPluginList(Object requestOwner, PluginRefreshListener pluginRefreshListener) {
        refreshPluginList(AppRepository.pluginRepo(), requestOwner, pluginRefreshListener);
    }

    public static void refreshPluginList(AppRepository repository, PluginRefreshListener pluginRefreshListener) {
        refreshPluginList(repository, pluginRefreshListener, pluginRefreshListener);
    }

    public static synchronized void refreshPluginList(AppRepository repository, Object requestOwner,
                                                       PluginRefreshListener pluginRefreshListener) {
        Objects.requireNonNull(requestOwner, "requestOwner");
        LoadRequest request = new LoadRequest();
        LoadRequest previous = scheduledRequests.put(requestOwner, request);
        if (previous != null && previous.future != null) {
            previous.future.cancel(true);
        }
        request.future = executorService.schedule(() -> {
            List<Plugin> list = loadPluginList(repository);
            synchronized (PluginInfoLoader.class) {
                if (scheduledRequests.get(requestOwner) != request) {
                    return;
                }
                scheduledRequests.remove(requestOwner);
            }
            pluginRefreshListener.listLoaded(list);
        }, 0, TimeUnit.SECONDS);
    }

    public static synchronized void killProcess() {
        scheduledRequests.values().forEach(LoadRequest::cancel);
        scheduledRequests.clear();
    }

    public static synchronized void killProcess(Object requestOwner) {
        LoadRequest request = scheduledRequests.remove(requestOwner);
        if (request != null) {
            request.cancel();
        }
    }

    static List<Plugin> refreshPluginListTask(AppRepository repository, PluginRefreshListener pluginRefreshListener) {
        List<Plugin> list = loadPluginList(repository);
        pluginRefreshListener.listLoaded(list);
        return list == null ? List.of() : list;
    }

    private static List<Plugin> loadPluginList(AppRepository repository) {
        try {
            return RepositoryClient.loadPluginList(repository);
        } catch (Exception e) {
            return null;
        }
    }

    private static final class LoadRequest {
        private ScheduledFuture<?> future;

        private void cancel() {
            if (future != null) {
                future.cancel(true);
            }
        }
    }
}
