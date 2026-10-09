package org.pacos.core.component.plugin.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.vaadin.flow.component.html.Span;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.proxy.PluginProxy;

public final class RemovePluginEvent {

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
        try {
            CompletableFuture<Boolean> stopFuture = pluginProxy.getPluginManager().stopPlugin(pluginDTO);
            stopFuture.thenAccept(stopped -> {
                if (!Boolean.TRUE.equals(stopped)) {
                    NotificationUtils.error(new IllegalStateException("Plugin could not be stopped: " + pluginDTO));
                    return;
                }
                if (!pluginProxy.getPluginState().canRun(pluginDTO)) {
                    NotificationUtils.error(new IllegalStateException("Plugin is not in a removable state: " + pluginDTO));
                    return;
                }
                pluginProxy.getPluginService().removePlugin(pluginDTO);
                pluginProxy.getPluginManager().removePlugin(pluginDTO);
                confirmEvent.finish();
            }).exceptionally(exception -> {
                Throwable cause = unwrap(exception);
                NotificationUtils.error(cause instanceof Exception e ? e : new IllegalStateException(cause));
                return null;
            });
            return true;
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception);
            return false;
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
