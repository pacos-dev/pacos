package org.pacos.core.component.menu;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.system.event.OpenUserSettingsEvent;
import org.pacos.core.system.event.ShowLogoutWindowEvent;
import org.pacos.core.system.proxy.AppProxy;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.pacos.core.system.window.OnboardingWindow;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.UI;

/**
 * Provides a compact entry point for user settings, appearance and help actions.
 */
public class UserPanelPopover extends Popover {

    private final AppProxy appProxy;
    private final UISystem uiSystem;
    private final Button appearanceAction;

    public UserPanelPopover(AppProxy appProxy, UISystem uiSystem) {
        this.appProxy = appProxy;
        this.uiSystem = uiSystem;
        this.appearanceAction = createAction("Appearance · Light", VaadinIcon.PALETTE, this::toggleAppearance);

        setWidth("300px");
        setPosition(PopoverPosition.BOTTOM_END);
        setOpenOnClick(true);
        setCloseOnOutsideClick(true);
        setModal(false);

        com.vaadin.flow.component.orderedlayout.VerticalLayout content =
                new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.setWidthFull();

        content.add(createAction("Change password", VaadinIcon.KEY, () -> {
                    setOpened(false);
                    OpenUserSettingsEvent.fireEvent(uiSystem, appProxy.getRegistryProxy());
                }));

        content.add(appearanceAction);
        updateAppearanceStatus();

        content.add(createAction("Documentation", VaadinIcon.BOOK, () -> {
                    setOpened(false);
                    UI.getCurrent().getPage().open("https://pacos.dev/docs/user", "_blank");
                }));

        if (OnboardingWindow.isSetupOwnerIdentity(UserSession.getCurrent(), appProxy.getRegistryProxy())) {
            content.add(createAction("Getting Started", VaadinIcon.INFO_CIRCLE, () -> {
                        setOpened(false);
                        OnboardingWindow.openManuallyForSetupOwner(appProxy.getRegistryProxy());
                    }));
        }

        Span separator = new Span();
        separator.setWidthFull();
        separator.getStyle().set("display", "block").set("height", "1px")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("margin", "var(--lumo-space-xs) 0");
        content.add(separator);

        Button logout = new Button("Logout", VaadinIcon.POWER_OFF.create(), event -> {
            setOpened(false);
            ShowLogoutWindowEvent.fireEvent(uiSystem);
        });
        logout.setWidthFull();
        logout.getStyle().set("justify-content", "flex-start")
                .set("color", "var(--lumo-error-text-color)");
        content.add(logout);

        add(content);
    }

    private Button createAction(String title, VaadinIcon icon, Runnable action) {
        Button item = new Button(title, icon.create(), event -> action.run());
        item.setWidthFull();
        item.getStyle().set("justify-content", "flex-start")
                .set("text-align", "left")
                .set("font-weight", "500");
        item.getElement().setAttribute("theme", "tertiary");
        return item;
    }

    private void toggleAppearance() {
        String current = UI.getCurrent().getElement().getAttribute("theme");
        UITheme next = UITheme.DARK.getThemeName().equals(current) ? UITheme.LIGHT : UITheme.DARK;
        ThemeManager.setTheme(next);
        updateAppearanceStatus();
    }

    private void updateAppearanceStatus() {
        String current = UI.getCurrent().getElement().getAttribute("theme");
        appearanceAction.setText("Appearance · "
                + (UITheme.DARK.getThemeName().equals(current) ? "Dark" : "Light"));
    }
}
