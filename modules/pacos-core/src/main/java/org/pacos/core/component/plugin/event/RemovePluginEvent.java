package org.pacos.core.component.plugin.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemovePluginEvent {

    private static final Logger LOG = LoggerFactory.getLogger(RemovePluginEvent.class);

    private RemovePluginEvent() {
    }

    public static void fireEvent(PluginProxy pluginProxy, PluginDTO pluginDTO, OnRemoveFinishEvent confirmEvent) {
        final ConfirmationWindowConfig config =
                new ConfirmationWindowConfig(() -> onConfirmEvent(pluginProxy, pluginDTO, confirmEvent));
        config.setContent(VerticalLayoutUtils.defaults(
                new Span("Are you sure?"),
                new Span("The plugin will be disabled and removed (only the library) but its data will " +
                        "be preserved (files, logs, database)")
        ));
        UISystem.getCurrent().getWindowManager().showModalWindow(config);
    }

    static boolean onConfirmEvent(PluginProxy pluginProxy, PluginDTO pluginDTO, OnRemoveFinishEvent confirmEvent) {
        UI ui = UI.getCurrent();
        try {
            CompletableFuture<Boolean> stopFuture = pluginProxy.getPluginManager().stopPlugin(pluginDTO);
            stopFuture.thenAccept(stopped -> {
                if (!Boolean.TRUE.equals(stopped)) {
                    notifyError(ui, new IllegalStateException("Plugin could not be stopped: " + pluginDTO));
                    return;
                }
                if (!pluginProxy.getPluginState().canRun(pluginDTO)) {
                    notifyError(ui, new IllegalStateException("Plugin is not in a removable state: " + pluginDTO));
                    return;
                }
                pluginProxy.getPluginManager().removePlugin(pluginDTO);
                pluginProxy.getPluginService().removePlugin(pluginDTO);
                confirmEvent.finish();
            }).exceptionally(exception -> {
                notifyError(ui, unwrap(exception));
                return null;
            });
            return true;
        } catch (RuntimeException exception) {
            notifyError(ui, exception);
            return false;
        }
    }

    private static void notifyError(UI ui, Throwable cause) {
        Exception exception = cause instanceof Exception e ? e : new IllegalStateException(cause);
        if (ui == null) {
            LOG.error("Plugin removal failed", exception);
            return;
        }
        try {
            ui.access(() -> NotificationUtils.error(exception));
        } catch (RuntimeException notificationFailure) {
            exception.addSuppressed(notificationFailure);
            LOG.error("Plugin removal failed", exception);
        }
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable cause = throwable;
        while ((cause instanceof CompletionException || cause instanceof java.util.concurrent.ExecutionException)
                && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
