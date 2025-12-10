package org.pacos.core.component.plugin.event;

import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.component.html.Span;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginState;
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
            CompletableFuture<Boolean> completableFuture = pluginProxy.getPluginManager().stopPlugin(pluginDTO);
            completableFuture.thenAccept(result -> {
                if (Boolean.TRUE.equals(result) && PluginState.canRun(pluginDTO)) {
                    pluginProxy.getPluginService().removePlugin(pluginDTO);
                    pluginProxy.getPluginManager().removePlugin(pluginDTO);
                    confirmEvent.finish();
                }
            });
            return true;
        } catch (Exception e) {
            NotificationUtils.error(e);
            return false;
        }
    }
}

