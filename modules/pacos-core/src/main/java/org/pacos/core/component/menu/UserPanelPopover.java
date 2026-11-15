package org.pacos.core.component.menu;

import org.pacos.base.component.Style;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.system.event.OpenUserSettingsEvent;
import org.pacos.core.system.proxy.AppProxy;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.pacos.core.system.window.OnboardingWindow;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;

/**
 * Provides a compact entry point for user settings, appearance and help actions.
 */
public class UserPanelPopover extends Popover {

    private final AppProxy appProxy;
    private final UISystem uiSystem;
    private final Div appearanceAction;
    private Span appearanceDescription;

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
        content.addClassName("user-panel-content");
        content.getStyle().set(Style.USER_SELECT.value(), "none");

        content.add(createAction("Change password", "Update your account password", VaadinIcon.KEY, () -> {
                    setOpened(false);
                    OpenUserSettingsEvent.fireEvent(uiSystem, appProxy.getRegistryProxy());
                }));

        content.add(appearanceAction);
        updateAppearanceStatus();

        Div divider = new Div();
        divider.addClassName("user-panel-divider");
        divider.getElement().setAttribute("role", "separator");
        content.add(divider);

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

        add(content);
    }

    private Div createAction(String title, VaadinIcon icon, Runnable action) {
        Div item = new Div();
        item.setWidthFull();
        item.getStyle().set(Style.USER_SELECT.value(), "none")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer");
        item.addClassName("user-panel-action");
        Icon actionIcon = icon.create();
        actionIcon.getStyle().set("width", "1rem").set("height", "1rem")
                .set("flex-shrink", "0").set("color", "var(--lumo-secondary-text-color)");
        Span titleText = new Span(title);
        titleText.getStyle().set("font-weight", "600");
        item.add(actionIcon, titleText);
        item.addClickListener(event -> action.run());
        item.addKeyDownListener(Key.ENTER, event -> action.run());
        item.addKeyDownListener(Key.SPACE, event -> action.run());
        item.getElement().setAttribute("role", "button");
        item.getElement().setAttribute("tabindex", "0");
        item.getElement().setAttribute("aria-label", title);
        return item;
    }

    private Div createAction(String title, String description, VaadinIcon icon, Runnable action) {
        Div item = new Div();
        item.setWidthFull();
        item.getStyle().set(Style.USER_SELECT.value(), "none")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer");
        item.addClassName("user-panel-action");
        com.vaadin.flow.component.icon.Icon actionIcon = icon.create();
        actionIcon.getStyle().set("width", "1rem").set("height", "1rem")
                .set("flex-shrink", "0").set("color", "var(--lumo-secondary-text-color)");
        Span titleText = new Span(title);
        titleText.getStyle().set("font-weight", "600");
        Span descriptionText = new Span(description);
        if ("Appearance".equals(title)) {
            appearanceDescription = descriptionText;
        }
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

    /** Toggles the current UI theme and refreshes the active-theme description. */
    private void toggleAppearance() {
        String current = UI.getCurrent().getElement().getAttribute("theme");
        UITheme next = UITheme.DARK.getThemeName().equals(current) ? UITheme.LIGHT : UITheme.DARK;
        ThemeManager.setTheme(next);
        updateAppearanceStatus();
    }

    /** Updates the appearance action description to match the current UI theme. */
    private void updateAppearanceStatus() {
        String current = UI.getCurrent().getElement().getAttribute("theme");
        appearanceDescription.setText("Currently active: "
                + (UITheme.DARK.getThemeName().equals(current) ? "Dark" : "Light"));
    }
}
