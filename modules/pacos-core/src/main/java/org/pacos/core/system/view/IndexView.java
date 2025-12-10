package org.pacos.core.system.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.config.session.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@RouteAlias("home")
@RouteAlias("main")
@RouteAlias("index")
public class IndexView extends Div implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(IndexView.class);
    private final transient UserProxyService userProxyService;
    private final transient RegistryProxy registryProxy;

    @Autowired
    private IndexView(UserProxyService userProxyService, RegistryProxy registryProxy) {
        this.userProxyService = userProxyService;
        this.registryProxy = registryProxy;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (!registryProxy.isInstalled()) {
            LOG.info("Redirect to installation page");
            beforeEnterEvent.forwardTo(InstallerView.class);
            return;
        }

        if (!UserSessionService.isLogIn(userProxyService) && UserSessionService.autoLogin(userProxyService)) {
            LOG.debug("Redirect to desktop page. Auto login based on token");
            beforeEnterEvent.forwardTo(DesktopView.class);
        } else if (!UserSessionService.isLogIn(userProxyService)) {
            LOG.debug("Redirect to login page");
            beforeEnterEvent.forwardTo(LoginView.class);
        } else {
            LOG.debug("Redirect to desktop page");
            beforeEnterEvent.forwardTo(DesktopView.class);
        }
    }
}
