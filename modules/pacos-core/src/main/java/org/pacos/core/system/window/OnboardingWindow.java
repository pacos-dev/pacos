package org.pacos.core.system.window;

import org.pacos.base.component.Style;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.menu.ApplicationsModal;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.config.OnboardingWindowConfig;
import org.pacos.core.component.user.view.config.UserPasswordConfig;
import org.pacos.core.component.variable.view.plugin.VariablePlugin;
import org.pacos.core.system.event.OpenVariablePluginEvent;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
    private final PluginState pluginState;

    public OnboardingWindow(OnboardingWindowConfig config, RegistryProxy registryProxy, PluginState pluginState) {
        super(config);
        this.registryProxy = registryProxy;
        this.pluginState = pluginState;
        UI.getCurrent().getPage().addStyleSheet("/css/onboarding.css");
        addClassName("onboarding-window");
        setWidth(850, Unit.PIXELS);
        setHeight(750, Unit.PIXELS);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        getWindowHeader().getMinimizeBtn().setVisible(false);
        getWindowHeader().getExpandBtn().setVisible(false);
        getWindowHeader().getCloseBtn().setVisible(false);

        VerticalLayout content = new VerticalLayout();
        content.addClassName("onboarding-content");
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidthFull();
        content.getStyle().set(Style.USER_SELECT.value(), "none");

        Div hero = new Div();
        hero.addClassName("onboarding-hero");

        VerticalLayout heroCopy = new VerticalLayout();
        heroCopy.addClassName("onboarding-hero-copy");
        heroCopy.setPadding(false);
        heroCopy.setSpacing(false);
        heroCopy.setWidth("min(650px, 58%)");

        Span eyebrow = new Span("WELCOME TO PACOS");
        eyebrow.addClassName("onboarding-eyebrow");

        Span heroTitle = new Span("Your workspace. Your tools. Your way.");
        heroTitle.addClassName("onboarding-hero-title");

        Paragraph heroDescription = new Paragraph(
                "Everything you need to work with your applications — gathered in one place. Choose a card and make PacOS yours.");
        heroDescription.addClassName("onboarding-hero-description");

        heroCopy.add(eyebrow, heroTitle, heroDescription);
        hero.add(heroCopy);
        content.add(hero);

        HorizontalLayout sectionHeading = new HorizontalLayout();
        sectionHeading.addClassName("onboarding-section-heading");
        sectionHeading.setWidthFull();
        sectionHeading.setAlignItems(FlexComponent.Alignment.CENTER);
        Span sectionTitle = new Span("Explore your workspace");
        sectionTitle.addClassName("onboarding-section-title");
        Span sectionHint = new Span("SELECT A CARD TO OPEN");
        sectionHint.addClassName("onboarding-section-hint");
        sectionHeading.add(sectionTitle, sectionHint);
        content.add(sectionHeading);

        Div tiles = new Div();
        tiles.addClassName("onboarding-tiles");
        tiles.setWidthFull();
        tiles.add(createTile(VaadinIcon.FILE_SEARCH, "Explore applications", "01",
                "Discover the tools available in your PacOS workspace.",
                "Open the application launcher and see what's available.",
                () -> new ApplicationsModal(pluginState).open()));
        tiles.add(createTile(VaadinIcon.USER, "Your account", "02",
                "Keep your account details and personal settings up to date.",
                "Review your profile and preferences.", () -> showWindow(UserPasswordConfig.class)));
        tiles.add(createTile(VaadinIcon.PUZZLE_PIECE, "Extend PacOS", "03",
                "Add the tools that fit your workflow and your team.",
                "Browse and install extensions in App Center.", () -> showWindow(AppStoreWindowConfig.class)));
        tiles.add(createTile(VaadinIcon.PALETTE, "Make it yours", "04",
                "Choose the appearance that works best for you.",
                "Switch between light and dark mode.", this::toggleAppearance));
        tiles.add(createTile(VaadinIcon.CODE, "User variables", "05",
                "Reuse values across your work and keep them in one place.",
                "Open the {v} shortcut in the desktop toolbar.",
                this::openVariablePlugin));
        tiles.add(createTile(VaadinIcon.BOOK, "Documentation", "06",
                "Find practical guides and detailed information about PacOS.",
                "Open the online documentation.",
                () -> UI.getCurrent().getPage().open("https://pacos.dev/docs/user", "_blank")));
        content.add(tiles);
        getContent().add(content);

        Button closeButton = new Button("Close guide", event -> confirmDismissal());
        closeButton.addClassName("onboarding-close-button");
        getFooter().add(closeButton);
    }

    private String tileAccent(String title) {
        return switch (title) {
            case "Explore applications" -> "#2788f5";
            case "Your account" -> "#8b72e8";
            case "Extend PacOS" -> "#16a6a1";
            case "Make it yours" -> "#d58a32";
            case "User variables" -> "#3b9b78";
            default -> "#477fe8";
        };
    }

    private Div createTile(VaadinIcon iconType, String title, String number,
                           String description, String actionHint, Runnable action) {
        String accent = tileAccent(title);

        Div tile = new Div();
        tile.addClassName("onboarding-tile");
        tile.getStyle().set(Style.USER_SELECT.value(), "none");
        tile.getStyle().set("--tile-accent", accent);
        tile.getElement().setAttribute("role", "button");
        tile.getElement().setAttribute("tabindex", "0");
        tile.getElement().setAttribute("aria-label", title + ". " + actionHint);

        Icon watermarkIcon = iconType.create();
        watermarkIcon.addClassName("onboarding-tile-watermark");
        tile.add(watermarkIcon);

        HorizontalLayout heading = new HorizontalLayout();
        heading.addClassName("onboarding-tile-heading");
        heading.setWidthFull();
        heading.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon icon = iconType.create();
        icon.addClassName("onboarding-tile-icon");
        Div iconBadge = new Div(icon);
        iconBadge.addClassName("onboarding-tile-icon-badge");

        Span headingTitle = new Span(title);
        headingTitle.addClassName("onboarding-tile-title");
        Span index = new Span(number);
        index.addClassName("onboarding-tile-index");
        heading.add(iconBadge, headingTitle, index);

        Paragraph details = new Paragraph(description);
        details.addClassName("onboarding-tile-description");

        Span hint = new Span(actionHint);
        hint.addClassName("onboarding-tile-hint");
        Span arrow = new Span("→");
        arrow.addClassName("onboarding-tile-arrow");
        HorizontalLayout footer = new HorizontalLayout(hint, arrow);
        footer.addClassName("onboarding-tile-footer");
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        tile.add(heading, details, footer);
        tile.addClickListener(event -> action.run());
        tile.addClickShortcut(Key.ENTER);
        tile.addClickShortcut(Key.SPACE);
        return tile;
    }

    private void openVariablePlugin() {
        VariablePlugin variablePlugin = ComponentUtil.getData(UI.getCurrent(), VariablePlugin.class);
        if (variablePlugin == null && !UserSession.getCurrent().getUser().isGuestSession()) {
            Notification.show("User variables are not available in this workspace.");
            return;
        }
        OpenVariablePluginEvent.fireEvent(uiSystem, variablePlugin, registryProxy);
    }

    private void toggleAppearance() {
        String currentTheme = UI.getCurrent().getElement().getAttribute("theme");
        UITheme nextTheme = UITheme.DARK.getThemeName().equals(currentTheme) ? UITheme.LIGHT : UITheme.DARK;
        ThemeManager.setTheme(nextTheme);
        Notification.show("Appearance changed to " + nextTheme.getThemeName() + " mode.");
    }

    /** Opens a configured PacOS window through the active UI system. */
    private void showWindow(Class<? extends WindowConfig> config) {
        uiSystem.getWindowManager().showWindow(config);
    }

    private void confirmDismissal() {
        Dialog confirmation = new Dialog();
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
        if (isSetupOwner(UserSession.getCurrent(), registryProxy)) {
            UISystem.getCurrent().getWindowManager().showWindow(OnboardingWindowConfig.class);
        }
    }

    /**
     * Opens the guide on demand for the setup owner, even after it has been dismissed.
     *
     * @param registryProxy system registry used to resolve the setup owner
     */
    public static void openManuallyForSetupOwner(RegistryProxy registryProxy) {
        if (isSetupOwnerIdentity(UserSession.getCurrent(), registryProxy)) {
            UISystem.getCurrent().getWindowManager().showWindow(OnboardingWindowConfig.class);
        } else {
            Notification.show("The getting started guide is available to the setup owner only.");
        }
    }

    /**
     * Checks whether the session belongs to the account that completed initial setup.
     *
     * @param session current user session
     * @param registryProxy system registry containing the setup owner identifier
     * @return {@code true} when the session belongs to the setup owner
     */
    public static boolean isSetupOwnerIdentity(UserSession session, RegistryProxy registryProxy) {
        if (session == null || session.getUser() == null || session.getUser().isGuestSession()) {
            return false;
        }
        Integer ownerId = registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null);
        return ownerId != null && ownerId == session.getUserId();
    }

    /**
     * Checks whether the setup owner should be shown the guide automatically.
     *
     * @param session current user session
     * @param registryProxy system registry containing onboarding state
     * @return {@code true} when the owner has not dismissed the guide
     */
    static boolean isSetupOwner(UserSession session, RegistryProxy registryProxy) {
        return isSetupOwnerIdentity(session, registryProxy)
                && !Boolean.TRUE.equals(registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false));
    }
}
