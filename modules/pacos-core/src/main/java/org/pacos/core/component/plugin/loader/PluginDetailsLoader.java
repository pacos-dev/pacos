package org.pacos.core.component.plugin.loader;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
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
    private static final Map<Object, LoadRequest> scheduledRequests = new IdentityHashMap<>();

    private PluginDetailsLoader() {
    }

    public static void loadPluginDetails(AppArtifact artifact, PluginDetailsRefreshListener listener) {
        loadPluginDetails(artifact, AppRepository.pluginRepo(), listener, listener);
    }

    public static void loadPluginDetails(AppArtifact artifact, AppRepository repository,
                                         PluginDetailsRefreshListener listener) {
        loadPluginDetails(artifact, repository, listener, listener);
    }

    public static synchronized void loadPluginDetails(AppArtifact artifact, AppRepository repository,
                                                       Object requestOwner, PluginDetailsRefreshListener listener) {
        Objects.requireNonNull(requestOwner, "requestOwner");
        LoadRequest request = new LoadRequest();
        LoadRequest previous = scheduledRequests.put(requestOwner, request);
        if (previous != null && previous.future != null) {
            previous.future.cancel(true);
        }
        Callable<PluginInfo> loadTask = () -> loadPluginDetailTask(artifact, repository, details -> {
            synchronized (PluginDetailsLoader.class) {
                if (scheduledRequests.get(requestOwner) != request) {
                    return;
                }
                scheduledRequests.remove(requestOwner);
            }
            listener.refreshed(details);
        });
        request.future = executorService.schedule(loadTask, 0, TimeUnit.SECONDS);
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

    static PluginInfo loadPluginDetailTask(AppArtifact artifact, AppRepository repository,
                                            PluginDetailsRefreshListener listener) {
        PluginInfo list = null;
        try {
            list = RepositoryClient.loadInfo(new AppRepositoryArtifact(artifact, repository));
        } catch (RuntimeException ignored) {
            LOG.error("Can't load plugin details: {}", artifact);
        }
        listener.refreshed(list);
        return list;
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
