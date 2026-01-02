package org.pacos.core.component.menu;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.component.Style;
import org.pacos.base.component.icon.IconClass;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.core.component.variable.view.plugin.VariablePlugin;
import org.pacos.core.system.event.OpenSystemInfoEvent;
import org.pacos.core.system.event.OpenUserSettingsEvent;
import org.pacos.core.system.event.OpenVariablePluginEvent;
import org.pacos.core.system.event.ShowLogoutWindowEvent;
import org.pacos.core.system.proxy.AppProxy;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;

import java.time.Instant;

public class MenuSystem extends Div {

    public MenuSystem(AppProxy appProxy) {
        setWidth("100%");
        getElement().setAttribute("theme", "menu-system");

        ApplicationsModal modal = new ApplicationsModal();
        final Button homeButton = new ButtonUtils("Applications", new Icon(VaadinIcon.SEARCH))
                .withClickListener(e -> modal.open())
                .withStyle(Style.USER_SELECT.value(), "none")
                .withClassName("menu-btn");
        UserSession userSession = UserSession.getCurrent();

        add(homeButton);

        final DivUtils clock = new DivUtils().withId("clock");
        String serverTime = Instant.now().toEpochMilli() + "";
        clock.getElement().setAttribute("time", serverTime);
        clock.setText("00:00");
        clock.getElement().setAttribute("title", "Server time");
        add(clock);

        final String btnClassName = "menu-btn";

        UISystem uiSystem = userSession.getUISystem();
        Icon powerOffBtn = VaadinIcon.POWER_OFF.create();
        if (appProxy.getRegistryProxy().isRestartRequired()) {
            powerOffBtn.addClassName(IconClass.RED_CIRCLE.getName());
        } else {
            UISystem.getCurrent().subscribe(ModuleEvent.RESTART_REQUIRED, e ->
                    getUI().ifPresent(ui -> ui.access(() -> powerOffBtn.addClassName(IconClass.RED_CIRCLE.getName()))));
        }

        add(new ButtonUtils(powerOffBtn).withClassName(btnClassName)
                .floatRight()
                .withTooltip("Logout")
                .withClickListener(e -> ShowLogoutWindowEvent.fireEvent(uiSystem)));

        add(new ButtonUtils(VaadinIcon.USER.create()).withClassName(btnClassName)
                .floatRight()
                .withTooltip("User settings")
                .withClickListener(e -> OpenUserSettingsEvent.fireEvent(uiSystem, appProxy.getRegistryProxy())));
        final ButtonUtils styleModeBtn = new ButtonUtils(UITheme.LIGHT.getIcon());
        add(styleModeBtn.withClassName(btnClassName)
                .floatRight()
                .withTooltip("UI mode")
                .withClickListener(e -> ThemeManager.changeTheme(styleModeBtn)));

        ButtonUtils buttonUtils = new ButtonUtils("{v}").withClassName(btnClassName)
                .floatRight().withStyle("min-width", "10px").withStyle("height", "19px")
                .withTooltip("User variables");
        add(buttonUtils);

        VariablePlugin variablePlugin = new VariablePlugin(appProxy.getUserVariableCollectionProxy(),
                appProxy.getUserVariableProxy(), appProxy.getUserProxyService());
        add(variablePlugin);

        buttonUtils.addClickListener(e -> OpenVariablePluginEvent.fireEvent(uiSystem, variablePlugin, appProxy.getRegistryProxy()));


        boolean updateAvailable = appProxy.getRegistryProxy().isSystemToUpdate();
        Icon icon = VaadinIcon.INFO_CIRCLE_O.create();
        if (updateAvailable) {
            icon.addClassName(IconClass.RED_CIRCLE.getName());
        }
        UISystem.getCurrent().subscribe(ModuleEvent.UPDATE_AVAILABLE, e -> icon.addClassName(IconClass.RED_CIRCLE.getName()));
        add(new ButtonUtils(icon).withClassName(btnClassName)
                .floatRight()
                .withTooltip("About PacOS")
                .withClickListener(e -> OpenSystemInfoEvent.fireEvent(uiSystem)));
    }

}
