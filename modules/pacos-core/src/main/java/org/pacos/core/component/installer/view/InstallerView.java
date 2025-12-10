package org.pacos.core.component.installer.view;

import java.util.Optional;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.installer.view.steps.Box;
import org.pacos.core.component.installer.view.steps.Step1Welcome;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.pacos.core.system.view.DesktopView;
import org.slf4j.LoggerFactory;

@PageTitle("Pac OS  - Installation")
@Route("install")
public class InstallerView extends Div implements BeforeEnterObserver {

    private final transient InstallerSettings settings = new InstallerSettings();
    private final transient InstallerService installerService;
    private final transient RegistryProxy registryProxy;
    private final Div content = new Div();
    public InstallerView(InstallerService installerService, RegistryProxy registryProxy) {
        this.installerService = installerService;
        this.registryProxy = registryProxy;

        UI.getCurrent().getPage().addStyleSheet("frontend/css/module.css");
        UI.getCurrent().getPage().addStyleSheet("frontend/css/install.css");

        ThemeManager.setTheme(UITheme.DARK);
        addClassName("installer");
        add(content);
        content.add(new Step1Welcome(this));
    }

    public InstallerSettings getSettings() {
        return settings;
    }

    public void setView(Box view) {
        content.removeAll();
        content.add(view);
    }

    public InstallerService getInstallerService() {
        return installerService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<String> installedFlag = registryProxy.readRegistry(RegistryName.INSTALLED);
        if (installedFlag.isPresent() && installedFlag.get().equals("true")) {
            LoggerFactory.getLogger(InstallerView.class).debug("Redirect to desktop page");
            beforeEnterEvent.forwardTo(DesktopView.class);
        }
    }

}
