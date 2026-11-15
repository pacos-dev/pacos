package org.pacos.core.system.window;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.menu.ApplicationsModal;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.config.OnboardingWindowConfig;
import org.pacos.core.component.user.view.config.UserConfig;
import org.pacos.core.component.variable.view.plugin.VariablePlugin;
import org.pacos.core.system.event.OpenVariablePluginEvent;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
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

    public OnboardingWindow(OnboardingWindowConfig config, RegistryProxy registryProxy) {
        super(config);
        this.registryProxy = registryProxy;
        setWidth(850, Unit.PIXELS);
        setHeight(750, Unit.PIXELS);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        getWindowHeader().getMinimizeBtn().setVisible(false);
        getWindowHeader().getExpandBtn().setVisible(false);
        getWindowHeader().getCloseBtn().setVisible(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidthFull();
        content.getStyle().set("gap", "var(--lumo-space-l)");

        Div hero = new Div();
        hero.getStyle().set("position", "relative")
                .set("display", "flex")
                .set("align-items", "center")
                .set("width", "100%")
                .set("min-height", "260px")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("border-radius", "18px")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("background-image", "linear-gradient(90deg, rgba(3, 16, 38, .82) 0%, rgba(3, 16, 38, .66) 38%, rgba(3, 16, 38, .08) 72%), url('/img/onboarding-hero.jpeg')")
                .set("background-size", "cover")
                .set("background-position", "center");

        VerticalLayout heroCopy = new VerticalLayout();
        heroCopy.setPadding(false);
        heroCopy.setSpacing(false);
        heroCopy.setWidth("min(650px, 58%)");
        heroCopy.getStyle().set("padding", "34px 40px")
                .set("gap", "12px")
                .set("position", "relative")
                .set("z-index", "1");

        Span eyebrow = new Span("WELCOME TO PACOS");
        eyebrow.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "800")
                .set("letter-spacing", "0.24em")
                .set("color", "rgba(220, 235, 255, .82)");

        Span heroTitle = new Span("Your workspace. Your tools. Your way.");
        heroTitle.getStyle().set("font-size", "clamp(1.65rem, 2.5vw, 2.3rem)")
                .set("font-weight", "800")
                .set("line-height", "1.12")
                .set("letter-spacing", "-0.035em")
                .set("color", "#ffffff")
                .set("max-width", "15em");

        Paragraph heroDescription = new Paragraph(
                "Everything you need to work with your applications — gathered in one place. Choose a card and make PacOS yours.");
        heroDescription.getStyle().set("margin", "0")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("line-height", "1.55")
                .set("color", "rgba(235, 242, 255, .88)")
                .set("max-width", "38em");

        heroCopy.add(eyebrow, heroTitle, heroDescription);
        hero.add(heroCopy);
        content.add(hero);

        HorizontalLayout sectionHeading = new HorizontalLayout();
        sectionHeading.setWidthFull();
        sectionHeading.setAlignItems(FlexComponent.Alignment.CENTER);
        Span sectionTitle = new Span("Explore your workspace");
        sectionTitle.getStyle().set("font-size", "var(--lumo-font-size-l)").set("font-weight", "700");
        Span sectionHint = new Span("SELECT A CARD TO OPEN");
        sectionHint.getStyle().set("margin-left", "auto").set("font-size", "var(--lumo-font-size-xs)")
                .set("letter-spacing", "0.08em").set("color", "var(--lumo-secondary-text-color)");
        sectionHeading.add(sectionTitle, sectionHint);
        content.add(sectionHeading);

        Div tiles = new Div();
        tiles.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(3, minmax(0, 1fr))")
                .set("gap", "var(--lumo-space-m)");
        tiles.setWidthFull();
        tiles.add(createTile(VaadinIcon.FILE_SEARCH, "Explore applications", "01",
                "Discover the tools available in your PacOS workspace.",
                "Open the application launcher and see what's available.",
                () -> new ApplicationsModal(new PluginState()).open()));
        tiles.add(createTile(VaadinIcon.USER, "Your account", "02",
                "Keep your account details and personal settings up to date.",
                "Review your profile and preferences.", () -> showWindow(UserConfig.class)));
        tiles.add(createTile(VaadinIcon.PUZZLE_PIECE, "Extend PacOS", "03",
                "Add the tools that fit your workflow and your team.",
                "Browse and install extensions in App Center.", () -> showWindow(AppStoreWindowConfig.class)));
        tiles.add(createTile(VaadinIcon.PALETTE, "Make it yours", "04",
                "Choose the appearance that works best for you.",
                "Switch between light and dark mode.", this::toggleAppearance));
        tiles.add(createTile(VaadinIcon.CODE, "User variables", "05",
                "Reuse values across your work and keep them in one place.",
                "Open the {v} shortcut in the desktop toolbar.",
                () -> OpenVariablePluginEvent.fireEvent(uiSystem, (VariablePlugin) UserSession.getCurrent().getFromSession("variablePlugin"), registryProxy)));
        tiles.add(createTile(VaadinIcon.BOOK, "Documentation", "06",
                "Find practical guides and detailed information about PacOS.",
                "Open the online documentation.",
                () -> UI.getCurrent().getPage().open("https://pacos.dev/docs/user", "_blank")));
        content.add(tiles);
        getContent().add(content);

        Button closeButton = new Button("Close guide", event -> confirmDismissal());
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
        boolean darkTheme = "dark".equals(UI.getCurrent().getElement().getAttribute("theme"));

        Div tile = new Div();
        tile.getStyle().set("position", "relative").set("isolation", "isolate")
                .set("display", "flex").set("flex-direction", "column")
                .set("gap", "12px").set("padding", "16px 18px 18px")
                .set("min-height", "164px").set("box-sizing", "border-box")
                .set("overflow", "hidden").set("border-radius", "18px")
                .set("border", "1px solid " + (darkTheme ? "rgba(255,255,255,.13)" : "rgba(25,35,50,.12)"))
                .set("border-top", "4px solid " + accent)
                .set("background", darkTheme
                        ? "linear-gradient(145deg, color-mix(in srgb, " + accent + " 13%, #292d33), #202329 72%)"
                        : "linear-gradient(145deg, color-mix(in srgb, " + accent + " 9%, #ffffff), #f3f5f8 78%)")
                .set("box-shadow", darkTheme
                        ? "0 8px 22px rgba(0,0,0,.22), inset 0 1px 0 rgba(255,255,255,.05)"
                        : "0 7px 20px rgba(25,35,50,.10), inset 0 1px 0 rgba(255,255,255,.9)")
                .set("cursor", "pointer")
                .set("transition", "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease");
        tile.getElement().setAttribute("role", "button");
        tile.getElement().setAttribute("tabindex", "0");
        tile.getElement().setAttribute("aria-label", title + ". " + actionHint);

        Icon watermarkIcon = iconType.create();
        watermarkIcon.setSize("88px");
        watermarkIcon.getStyle().set("position", "absolute").set("right", "-10px")
                .set("bottom", "-22px").set("opacity", darkTheme ? "0.075" : "0.055")
                .set("color", accent).set("z-index", "0").set("pointer-events", "none");
        tile.add(watermarkIcon);

        HorizontalLayout heading = new HorizontalLayout();
        heading.setWidthFull();
        heading.setAlignItems(FlexComponent.Alignment.CENTER);
        heading.getStyle().set("gap", "12px").set("position", "relative").set("z-index", "1");

        Icon icon = iconType.create();
        icon.setSize("22px");
        icon.getStyle().set("color", "#ffffff");
        Div iconBadge = new Div(icon);
        iconBadge.getStyle().set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("width", "44px").set("height", "44px")
                .set("flex-shrink", "0").set("border-radius", "13px")
                .set("background", accent)
                .set("box-shadow", "0 5px 14px " + accent + "55");

        Span headingTitle = new Span(title);
        headingTitle.getStyle().set("font-weight", "750")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", darkTheme ? "#f5f6f8" : "#202733");
        Span index = new Span(number);
        index.getStyle().set("margin-left", "auto").set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "800").set("letter-spacing", "0.08em")
                .set("color", darkTheme ? "rgba(255,255,255,.48)" : "rgba(32,39,51,.42)");
        heading.add(iconBadge, headingTitle, index);

        Paragraph details = new Paragraph(description);
        details.getStyle().set("margin", "0").set("max-width", "32em")
                .set("font-size", "var(--lumo-font-size-s)").set("line-height", "1.4")
                .set("color", darkTheme ? "rgba(235,239,245,.72)" : "#687386")
                .set("position", "relative").set("z-index", "1");

        Span hint = new Span(actionHint);
        hint.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("font-weight", "700")
                .set("color", darkTheme ? "#f4f6f9" : "#303a49");
        Span arrow = new Span("→");
        arrow.getStyle().set("font-size", "1.25rem").set("font-weight", "700")
                .set("margin-left", "auto").set("color", accent);
        HorizontalLayout footer = new HorizontalLayout(hint, arrow);
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle().set("margin-top", "auto").set("padding-top", "4px")
                .set("position", "relative").set("z-index", "1");

        tile.add(heading, details, footer);
        tile.addClickListener(event -> action.run());

        tile.getElement().addEventListener("mouseenter", event -> tile.getStyle()
                .set("transform", "translateY(-4px)")
                .set("box-shadow", darkTheme
                        ? "0 15px 30px rgba(0,0,0,.34), 0 0 0 1px " + accent + "66"
                        : "0 14px 28px rgba(25,35,50,.16), 0 0 0 1px " + accent + "88"));
        tile.getElement().addEventListener("mouseleave", event -> tile.getStyle()
                .remove("transform")
                .set("box-shadow", darkTheme
                        ? "0 8px 22px rgba(0,0,0,.22), inset 0 1px 0 rgba(255,255,255,.05)"
                        : "0 7px 20px rgba(25,35,50,.10), inset 0 1px 0 rgba(255,255,255,.9)"));
        tile.getElement().addEventListener("focus", event -> tile.getStyle()
                .set("outline", "2px solid " + accent).set("outline-offset", "3px"));
        tile.getElement().addEventListener("blur", event -> tile.getStyle()
                .remove("outline").remove("outline-offset"));
        return tile;
    }

    private void toggleAppearance() {
        String currentTheme = UI.getCurrent().getElement().getAttribute("theme");
        UITheme nextTheme = UITheme.DARK.getThemeName().equals(currentTheme) ? UITheme.LIGHT : UITheme.DARK;
        ThemeManager.setTheme(nextTheme);
        Notification.show("Appearance changed to " + nextTheme.getThemeName() + " mode.");
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
        if (isSetupOwner(UserSession.getCurrent(), registryProxy)) {
            UISystem.getCurrent().getWindowManager().showWindow(OnboardingWindowConfig.class);
        }
    }

    static boolean isSetupOwner(UserSession session, RegistryProxy registryProxy) {
        if (session == null || session.getUser() == null || session.getUser().isGuestSession()) {
            return false;
        }
        Integer ownerId = registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null);
        return ownerId != null && ownerId == session.getUserId()
                && !Boolean.TRUE.equals(registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false));
    }
}
