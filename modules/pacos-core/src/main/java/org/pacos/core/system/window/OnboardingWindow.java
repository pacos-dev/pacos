package org.pacos.core.system.window;

import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.config.OnboardingWindowConfig;
import org.pacos.core.component.user.view.config.UserConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Presents the first-run guide to the user who owns the initial PacOS setup.
 * The dismissal state is stored globally and can only be changed by that owner.
 */
@Component
@Scope("prototype")
public class OnboardingWindow extends DesktopWindow {

    private final RegistryProxy registryProxy;

    public OnboardingWindow(OnboardingWindowConfig config, RegistryProxy registryProxy) {
        super(config);
        this.registryProxy = registryProxy;
        setWidth("min(1100px, 92vw)");
        setHeight("min(760px, 90vh)");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.add(new Paragraph("Your PacOS environment is ready. Explore the areas below in any order. "
                + "The guide does not change your configuration."));
        content.add(new Paragraph("Choose an area to get started. You can return to this guide at any time."));

        Div tiles = new Div();
        tiles.getStyle().set("display", "grid");
        tiles.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(260px, 1fr))");
        tiles.getStyle().set("gap", "var(--lumo-space-m)");
        tiles.setWidthFull();

        tiles.add(createTile(VaadinIcon.FILE_SEARCH, "Explore applications",
                "Discover the tools available in your PacOS installation. Open Applications from the desktop toolbar."));
        tiles.add(createTile(VaadinIcon.USER, "Configure your account",
                "Review your account details and personal settings.",
                "Open user settings", () -> showWindow(UserConfig.class)));
        tiles.add(createTile(VaadinIcon.PUZZLE_PIECE, "Install extensions",
                "Browse available extensions and install the tools required by your team.",
                "Open App Center", () -> showWindow(AppStoreWindowConfig.class)));
        tiles.add(createTile(VaadinIcon.PALETTE, "Choose your appearance",
                "Switch between light and dark themes using the appearance control in the user panel."));
        tiles.add(createTile(VaadinIcon.CODE, "Review user variables",
                "Manage variables available to your user with the {v} shortcut in the desktop toolbar."));
        Div documentationTile = createTile(VaadinIcon.BOOK, "Read the documentation",
                "Find detailed guides, explanations and instructions for working with PacOS.");
        Anchor documentationLink = new Anchor("https://pacos.dev/docs/user", "Browse documentation");
        documentationLink.setTarget("_blank");
        documentationLink.getElement().setAttribute("rel", "noopener noreferrer");
        documentationLink.getStyle().set("margin-top", "auto").set("align-self", "flex-start");
        documentationTile.add(documentationLink);
        tiles.add(documentationTile);

        content.add(tiles);

        Anchor websiteLink = new Anchor("https://pacos.dev", "PacOS website");
        websiteLink.setTarget("_blank");
        websiteLink.getElement().setAttribute("rel", "noopener noreferrer");
        content.add(websiteLink);
        getContent().add(content);

        Button closeButton = new Button("Close guide", event -> confirmDismissal());
        getFooter().add(closeButton);
    }

    private Div createTile(VaadinIcon iconType, String title, String description) {
        return createTile(iconType, title, description, null, null);
    }

    private Div createTile(VaadinIcon iconType, String title, String description,
                           String actionLabel, Runnable action) {
        Div tile = new Div();
        tile.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-s)")
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)")
                .set("min-height", "160px")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "border-color 120ms ease, box-shadow 120ms ease");

        HorizontalLayout heading = new HorizontalLayout();
        heading.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "var(--lumo-primary-color)");
        Span headingTitle = new Span(title);
        headingTitle.getStyle().set("font-weight", "600");
        heading.add(icon, headingTitle);

        Paragraph details = new Paragraph(description);
        details.getStyle().set("margin", "0");
        tile.add(heading, details);

        if (actionLabel != null) {
            Button actionButton = new Button(actionLabel, event -> action.run());
            actionButton.getStyle().set("margin-top", "auto").set("align-self", "flex-start");
            tile.add(actionButton);
        }
        return tile;
    }

    private void showWindow(Class<? extends org.pacos.base.window.config.WindowConfig> config) {
        uiSystem.getWindowManager().showWindow(config);
    }

    private void confirmDismissal() {
        com.vaadin.flow.component.dialog.Dialog confirmation = new com.vaadin.flow.component.dialog.Dialog();
        confirmation.setHeaderTitle("Skip the getting started guide?");
        confirmation.add(new Paragraph("You can return to this guide later from the user panel."));
        Button cancel = new Button("Keep guide", event -> confirmation.close());
        Button confirm = new Button("Skip guide", event -> {
            registryProxy.saveRegistry(RegistryName.ONBOARDING_DISMISSED, true);
            confirmation.close();
            shutDown();
        });
        confirmation.getFooter().add(cancel, confirm);
        confirmation.open();
    }

    /**
     * Opens the guide automatically when the current session belongs to setup owner.
     */
    public static void openForSetupOwner(RegistryProxy registryProxy) {
        UserSession session = UserSession.getCurrent();
        if (session == null || session.getUser() == null || session.getUser().isGuestSession()) {
            return;
        }
        Integer ownerId = registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null);
        if (ownerId == null || ownerId != session.getUserId()
                || Boolean.TRUE.equals(registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false))) {
            return;
        }
        UserSession.getCurrent().getUISystem().getWindowManager()
                .showWindow(OnboardingWindowConfig.class);
    }
}
