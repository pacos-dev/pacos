package org.pacos.core.system.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.view.login.RegisterFormView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Pac OS - Register new account")
@Route(value = "register")
public class RegisterView extends FlexLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterView.class);
    private final transient UserProxyService userProxyService;
    private final transient RegistryProxy registryProxy;

    @Autowired
    public RegisterView(RegistryProxy registryProxy, UserProxyService userProxyService) {
        this.userProxyService = userProxyService;
        this.registryProxy = registryProxy;
        UI.getCurrent().getPage().addStyleSheet("frontend/css/login.css");

        final PacOSBackground background = new PacOSBackground();
        add(background);
        if (registryProxy.isRegistrationPanelEnabled()) {
            background.getWindow().add(RegisterFormView.build(userProxyService));
        }

    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (UserSessionService.isLogIn(userProxyService) || !registryProxy.isRegistrationPanelEnabled()
                || !registryProxy.isInstalled()) {
            LOG.info("Redirect to index view from register view");
            beforeEnterEvent.forwardTo(IndexView.class);
        }
    }
}
