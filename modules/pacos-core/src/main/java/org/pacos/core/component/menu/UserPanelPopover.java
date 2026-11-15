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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.UI;

/**
 * Provides a compact entry point for user settings, appearance and help actions.
 */
public class UserPanelPopover extends Popover {

    private final AppProxy appProxy;
    private final UISystem uiSystem;
    private final Div appearanceAction;

    public UserPanelPopover(AppProxy appProxy, UISystem uiSystem) {
        this.appProxy = appProxy;
        this.uiSystem = uiSystem;
        this.appearanceAction = createAction("Appearance", "Switch between Light and Dark mode", VaadinIcon.PALETTE, this::toggleAppearance);

        setWidth("300px");
        setPosition(PopoverPosition.BOTTOM_END);
        setOpenOnClick(true);
        setCloseOnOutsideClick(true);
        setModal(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle().set("gap", "var(--lumo-space-xs)");
        content.setWidthFull();

        content.add(createAction("Change password", "Update your account password", VaadinIcon.KEY, () -> {
                    setOpened(false);
                    OpenUserSettingsEvent.fireEvent(uiSystem, appProxy.getRegistryProxy());
                }));

        content.add(appearanceAction);
        updateAppearanceStatus();

        content.add(createAction("Documentation", "Open PacOS user documentation", VaadinIcon.BOOK, () -> {
                    setOpened(false);
                    UI.getCurrent().getPage().open("https://pacos.dev/docs/user", "_blank");
                }));

        if (OnboardingWindow.isSetupOwnerIdentity(UserSession.getCurrent(), appProxy.getRegistryProxy())) {
            content.add(createAction("Getting Started", "Reopen the PacOS setup guide", VaadinIcon.INFO_CIRCLE, () -> {
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

    private Div createAction(String title, String description, VaadinIcon icon, Runnable action) {
        Div item = new Div();
        item.setWidthFull();
        item.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer")
                .set("transition", "background-color 120ms ease");
        com.vaadin.flow.component.icon.Icon actionIcon = icon.create();
        actionIcon.getStyle().set("width", "1.25rem").set("height", "1.25rem")
                .set("flex-shrink", "0").set("color", "var(--lumo-secondary-text-color)");
        Span titleText = new Span(title);
        titleText.getStyle().set("font-weight", "600");
        Span descriptionText = new Span(description);
        descriptionText.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");
        VerticalLayout labels = new VerticalLayout(titleText, descriptionText);
        labels.setPadding(false);
        labels.setSpacing(false);
        labels.setWidthFull();
        item.add(actionIcon, labels);
        item.addClickListener(event -> action.run());
        item.addKeyDownListener(Key.ENTER, event -> action.run());
        item.addKeyDownListener(Key.SPACE, event -> action.run());
        item.getElement().setAttribute("role", "button");
        item.getElement().setAttribute("tabindex", "0");
        item.getElement().setAttribute("aria-label", title + ". " + description);
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
        appearanceAction.getElement().setAttribute("title", "Appearance · "
                + (UITheme.DARK.getThemeName().equals(current) ? "Dark" : "Light"));
        appearanceAction.getChildren().filter(VerticalLayout.class::isInstance)
                .map(VerticalLayout.class::cast).findFirst()
                .ifPresent(labels -> labels.getChildren().filter(Span.class::isInstance)
                        .map(Span.class::cast).skip(1).findFirst()
                        .ifPresent(description -> description.setText("Currently active: "
                                + (UITheme.DARK.getThemeName().equals(current) ? "Dark" : "Light"))));
    }
}
