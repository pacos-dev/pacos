package org.pacos.core.component.plugin.view.window;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AppStoreWindow extends DesktopWindow {

    public AppStoreWindow(AppStoreWindowConfig moduleConfig, PluginProxy pluginProxy) {
        super(moduleConfig);
        setHeight(700, Unit.PIXELS);
        add(new AppStoreLayout(pluginProxy));

        addAttachListener(attachEvent ->UI.getCurrent().getPage().addStyleSheet("frontend/css/module.css"));
    }
}
