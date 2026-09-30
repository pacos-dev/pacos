package org.pacos.core.system.view;

import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.event.InitializeGuestSessionEvent;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("guest")
public class GuestSession extends Div implements BeforeEnterObserver {

    private final transient UserProxyService userProxyService;
    private final transient RegistryProxy registryProxy;
    private final transient UserSessionService userSessionService;

    public GuestSession(UserProxyService userProxyService, RegistryProxy registryProxy, UserSessionService userSessionService) {
        this.userProxyService = userProxyService;
        this.registryProxy = registryProxy;
        this.userSessionService = userSessionService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (!registryProxy.isInstalled()) {
            beforeEnterEvent.forwardTo(InstallerView.class);
        } else if (UserSessionService.isLogIn(userProxyService)) {
            beforeEnterEvent.forwardTo(DesktopView.class);
        } else if (registryProxy.isGuestMode()) {
            InitializeGuestSessionEvent.fireEvent(userSessionService, UI.getCurrent());
            beforeEnterEvent.forwardTo(DesktopView.class);
        } else {
            beforeEnterEvent.forwardTo(DesktopView.class);
        }
    }
}
