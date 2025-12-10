package org.pacos.core.system.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.SpanUtils;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.event.InitializeGuestSessionEvent;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.pacos.core.system.view.login.LoginFormView;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Pac OS - Login page")
@Route(value = "login")
public class LoginView extends FlexLayout implements BeforeEnterObserver {

    private final transient UserProxyService userProxyService;
    private final transient RegistryProxy registryProxy;

    @Autowired
    public LoginView(UserSessionService userService, RegistryProxy registryProxy, UserProxyService userProxyService) {
        this.userProxyService = userProxyService;
        this.registryProxy = registryProxy;
        UI.getCurrent().getPage().addStyleSheet("frontend/css/login.css");

        ThemeManager.setTheme(UITheme.LIGHT);

        final PacOSBackground background = new PacOSBackground();
        add(background);

        new LoginFormView(userService).build(background.getWindow());
        if (registryProxy.isGuestMode()) {
            background.getWindow().add(buildGuestBlock());
        }

        if (registryProxy.isRegistrationPanelEnabled()) {

            Span register = new SpanUtils("Register a new account");
            register.setClassName("register");
            register.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));
            background.getWindow().add(new DivUtils().withComponents(register));
        }
    }

    private Div buildGuestBlock() {
        Div div = new Div();
        Button singUpAsGuestBtn = new Button("Log in as a guest",
                e -> InitializeGuestSessionEvent.fireEvent(userProxyService, UI.getCurrent()));
        singUpAsGuestBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        singUpAsGuestBtn.addClassName("login-invert-btn");
        singUpAsGuestBtn.addClassName("login-guest-btn");

        div.add(singUpAsGuestBtn);

        return div;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (UserSessionService.isLogIn(userProxyService) || !registryProxy.isInstalled()) {
            LoggerFactory.getLogger(IndexView.class).info("Redirect to index view");
            beforeEnterEvent.forwardTo(IndexView.class);
        }
    }
}
