package org.pacos.core.component.user.view;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.view.config.UserGuestSessionConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserGuestSessionPanel extends DesktopWindow {

    public UserGuestSessionPanel(UserGuestSessionConfig moduleConfig, UserProxyService userProxyService) {
        super(moduleConfig);
        setSize(450, 300);
        setModality(ModalityMode.STRICT);
        setResizable(false);
        setDraggable(false);
        getWindowHeader().getRight().setVisible(false);
        FormLayout registerLayout = RegisterFormLayout.build(uiSystem, this, userProxyService);

        add(new VerticalLayout(registerLayout));

        allowCloseOnEsc();
    }

}
