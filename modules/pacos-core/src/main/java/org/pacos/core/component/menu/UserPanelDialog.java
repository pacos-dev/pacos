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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.UI;

/**
 * Provides a compact entry point for user settings, appearance and help actions.
 */
public class UserPanelDialog extends Popover {

    private final AppProxy appProxy;
    private final UISystem uiSystem;
    private final Span appearanceStatus;

    public UserPanelDialog(AppProxy appProxy, UISystem uiSystem) {
        this.appProxy = appProxy;
        this.uiSystem = uiSystem;
        this.appearanceStatus = new Span();

        setWidth("430px");
        setPosition(PopoverPosition.BOTTOM_END);
        setOpenOnClick(true);
        setCloseOnOutsideClick(true);
        setModal(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidthFull();
        content.getStyle().set("gap", "var(--lumo-space-s)");

        H3 heading = new H3("User Panel");
        heading.getStyle().set("margin", "0 0 var(--lumo-space-xs) 0");
        content.add(heading);

        H3 sectionHeading = new H3("Your PacOS");
        sectionHeading.getStyle().set("margin", "0 0 var(--lumo-space-xs) 0");
        content.add(sectionHeading);

        content.add(createAction("User Settings", "Manage your account details",
                VaadinIcon.USER, () -> {
                    setOpened(false);
                    OpenUserSettingsEvent.fireEvent(uiSystem, appProxy.getRegistryProxy());
                }));

        Div appearance = createAction("Appearance", "Switch between Light and Dark mode",
                VaadinIcon.PALETTE, this::toggleAppearance);
        appearanceStatus.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        VerticalLayout appearanceContent = new VerticalLayout(appearance, appearanceStatus);
        appearanceContent.setPadding(false);
        appearanceContent.setSpacing(false);
        appearanceContent.setWidthFull();
        content.add(appearanceContent);
        updateAppearanceStatus();

        content.add(createAction("Documentation", "Open PacOS user documentation",
                VaadinIcon.BOOK, () -> {
                    setOpened(false);
                    UI.getCurrent().getPage().open("https://pacos.dev/docs/user", "_blank");
                }));

        if (OnboardingWindow.isSetupOwnerIdentity(UserSession.getCurrent(), appProxy.getRegistryProxy())) {
            content.add(createAction("Getting Started", "Reopen the PacOS setup guide",
                    VaadinIcon.INFO_CIRCLE, () -> {
                        setOpened(false);
                        OnboardingWindow.openManuallyForSetupOwner(appProxy.getRegistryProxy());
                    }));
        }

        Div separator = new Div();
        separator.setWidthFull();
        separator.getStyle().set("height", "1px")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("margin", "var(--lumo-space-s) 0");
        content.add(separator);

        Button logout = new Button("Logout", VaadinIcon.POWER_OFF.create(), event -> {
            close();
            ShowLogoutWindowEvent.fireEvent(uiSystem);
        });
        logout.setWidthFull();
        logout.getStyle().set("justify-content", "flex-start")
                .set("color", "var(--lumo-error-text-color)");
        content.add(logout);

        add(content);
    }

    private Div createAction(String title, String description,
                             VaadinIcon icon, Runnable action) {
        Div tile = new Div();
        tile.setWidthFull();
        tile.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)")
                .set("min-height", "58px")
                .set("box-sizing", "border-box")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer")
                .set("transition", "background-color 120ms ease, border-color 120ms ease");
        com.vaadin.flow.component.icon.Icon actionIcon = icon.create();
        actionIcon.getStyle().set("width", "1.35rem")
                .set("height", "1.35rem")
                .set("flex-shrink", "0")
                .set("color", "var(--lumo-primary-text-color)");
        Span titleText = new Span(title);
        titleText.getStyle().set("font-weight", "700");
        Span descriptionText = new Span(description);
        descriptionText.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        VerticalLayout labels = new VerticalLayout(titleText, descriptionText);
        labels.setPadding(false);
        labels.setSpacing(false);
        labels.setAlignItems(FlexComponent.Alignment.START);
        labels.setFlexGrow(1);
        tile.add(actionIcon, labels);
        tile.addClickListener(event -> action.run());
        tile.getElement().setAttribute("role", "button");
        tile.getElement().setAttribute("tabindex", "0");
        tile.getElement().setAttribute("aria-label", title + ". " + description);
        return tile;
    }

    private void toggleAppearance() {
        String current = UI.getCurrent().getElement().getAttribute("theme");
        UITheme next = UITheme.DARK.getThemeName().equals(current) ? UITheme.LIGHT : UITheme.DARK;
        ThemeManager.setTheme(next);
        updateAppearanceStatus();
    }

    private void updateAppearanceStatus() {
        String current = UI.getCurrent().getElement().getAttribute("theme");
        appearanceStatus.setText("Currently active: "
                + (UITheme.DARK.getThemeName().equals(current) ? "Dark" : "Light"));
    }
}
