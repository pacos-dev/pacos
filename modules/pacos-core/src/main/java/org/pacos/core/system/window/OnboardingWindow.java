package org.pacos.core.system.window;

import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.menu.ApplicationsModal;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.config.OnboardingWindowConfig;
import org.pacos.core.component.user.view.config.UserConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import org.vaadin.addons.variablefield.modal.VariableModal;
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
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidthFull();
        content.getStyle().set("gap", "var(--lumo-space-l)");

        Div hero = new Div();
        hero.getStyle().set("display", "grid").set("grid-template-columns", "minmax(0, 1.35fr) minmax(240px, 0.8fr)")
                .set("align-items", "center").set("gap", "var(--lumo-space-l)")
                .set("width", "100%").set("min-height", "205px").set("box-sizing", "border-box")
                .set("padding", "var(--lumo-space-xl) var(--lumo-space-xxl)").set("border-radius", "20px")
                .set("background", "radial-gradient(circle at 85% 15%, rgba(93,220,238,.48), transparent 30%), linear-gradient(115deg, #102e59 0%, #145a9c 52%, #168eae 100%)")
                .set("color", "#ffffff").set("overflow", "hidden").set("position", "relative");
        Div heroCopy = new Div();
        Span eyebrow = new Span("WELCOME TO PACOS");
        eyebrow.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "800").set("letter-spacing", "0.18em").set("color", "#a9eaff");
        Span heading = new Span("Your workspace. Your tools. Your way.");
        heading.getStyle().set("display", "block").set("font-size", "clamp(1.45rem, 2.4vw, 2.15rem)")
                .set("font-weight", "800").set("line-height", "1.12").set("margin-top", "var(--lumo-space-s)")
                .set("max-width", "560px");
        Paragraph intro = new Paragraph("Everything you need to work with your applications — gathered in one place. Choose a card and make PacOS yours.");
        intro.getStyle().set("max-width", "560px").set("font-size", "var(--lumo-font-size-m)")
                .set("line-height", "1.55").set("margin", "var(--lumo-space-m) 0 0")
                .set("color", "rgba(255,255,255,0.88)");
        heroCopy.add(eyebrow, heading, intro);
        Div artwork = createHeroArtwork();
        hero.add(heroCopy, artwork);
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
                () -> ((VariableModal) uiSystem.getVariableManager()).open()));
        tiles.add(createTile(VaadinIcon.BOOK, "Documentation", "06",
                "Find practical guides and detailed information about PacOS.",
                "Open the online documentation.",
                () -> UI.getCurrent().getPage().open("https://pacos.dev/docs/user", "_blank")));
        content.add(tiles);
        getContent().add(content);

        Button closeButton = new Button("Close guide", event -> confirmDismissal());
        getFooter().add(closeButton);
    }

    private Div createHeroArtwork() {
        Div scene = new Div();
        scene.getStyle().set("height", "150px").set("min-width", "230px")
                .set("position", "relative").set("border-radius", "18px")
                .set("background", "linear-gradient(145deg, rgba(255,255,255,.13), rgba(255,255,255,.04))")
                .set("border", "1px solid rgba(255,255,255,.22)")
                .set("box-shadow", "0 18px 45px rgba(0,0,0,.18)")
                .set("transform", "rotate(-3deg)");
        Div window = new Div();
        window.getStyle().set("position", "absolute").set("left", "12%").set("top", "16%")
                .set("width", "76%").set("height", "68%").set("border-radius", "10px")
                .set("background", "rgba(12,35,67,.82)").set("border", "1px solid rgba(255,255,255,.28)")
                .set("box-shadow", "0 10px 25px rgba(0,0,0,.22)").set("padding", "10px")
                .set("box-sizing", "border-box");
        HorizontalLayout dots = new HorizontalLayout();
        dots.setSpacing(false);
        dots.getStyle().set("gap", "5px");
        for (String color : new String[]{"#ff8c78", "#ffd166", "#72dfb2"}) {
            Span dot = new Span();
            dot.getStyle().set("width", "7px").set("height", "7px").set("border-radius", "50%")
                    .set("background", color);
            dots.add(dot);
        }
        Div lineOne = artworkLine("68%");
        Div lineTwo = artworkLine("88%");
        Div lineThree = artworkLine("48%");
        Div appIcon = new Div(VaadinIcon.CUBES.create());
        appIcon.getStyle().set("position", "absolute").set("right", "-13px").set("bottom", "-14px")
                .set("width", "54px").set("height", "54px").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("border-radius", "16px").set("background", "#ffffff")
                .set("color", "#1676c5").set("box-shadow", "0 10px 24px rgba(0,0,0,.2)");
        appIcon.getElement().getChild(0).getStyle().set("width", "27px").set("height", "27px");
        VerticalLayout preview = new VerticalLayout(dots, lineOne, lineTwo, lineThree);
        preview.setPadding(false);
        preview.setSpacing(false);
        preview.getStyle().set("gap", "9px").set("margin-top", "7px");
        window.add(preview);
        scene.add(window, appIcon);
        return scene;
    }

    private Div artworkLine(String width) {
        Div line = new Div();
        line.getStyle().set("height", "6px").set("width", width).set("border-radius", "8px")
                .set("background", "rgba(185,225,255,.55)");
        return line;
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
        Div tile = new Div();
        tile.getStyle().set("display", "flex").set("flex-direction", "column")
                .set("gap", "var(--lumo-space-s)").set("padding", "var(--lumo-space-l)")
                .set("min-height", "218px").set("box-sizing", "border-box")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-top", "3px solid " + tileAccent(title)).set("border-radius", "16px")
                .set("background", "linear-gradient(145deg, var(--lumo-base-color), var(--lumo-contrast-5pct))")
                .set("box-shadow", "var(--lumo-box-shadow-xs)").set("cursor", "pointer")
                .set("transition", "transform 140ms ease, box-shadow 140ms ease, border-color 140ms ease");
        tile.getElement().setAttribute("role", "button");
        tile.getElement().setAttribute("tabindex", "0");
        tile.getElement().setAttribute("aria-label", title + ". " + actionHint);

        HorizontalLayout heading = new HorizontalLayout();
        heading.setWidthFull();
        heading.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = iconType.create();
        icon.setSize("23px");
        Div iconBadge = new Div(icon);
        iconBadge.getStyle().set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("width", "44px").set("height", "44px")
                .set("flex-shrink", "0").set("border-radius", "12px")
                .set("background", tileAccent(title) + "18")
                .set("color", tileAccent(title));
        Span headingTitle = new Span(title);
        headingTitle.getStyle().set("font-weight", "700").set("font-size", "var(--lumo-font-size-l)");
        Span index = new Span(number);
        index.getStyle().set("margin-left", "auto").set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "700").set("color", "var(--lumo-tertiary-text-color)");
        heading.add(iconBadge, headingTitle, index);

        Paragraph details = new Paragraph(description);
        details.getStyle().set("margin", "var(--lumo-space-s) 0 0")
                .set("line-height", "1.45").set("color", "var(--lumo-secondary-text-color)");
        Span hint = new Span(actionHint);
        hint.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");
        Span arrow = new Span("↗");
        arrow.getStyle().set("margin-left", "var(--lumo-space-xs)").set("color", "var(--lumo-primary-color)");
        HorizontalLayout footer = new HorizontalLayout(hint, arrow);
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle().set("margin-top", "auto").set("padding-top", "var(--lumo-space-m)");

        tile.add(heading, details, footer);
        tile.addClickListener(event -> action.run());
        tile.addKeyDownListener(Key.ENTER, event -> action.run());
        tile.addKeyDownListener(Key.SPACE, event -> action.run());
        tile.getElement().addEventListener("mouseenter", event -> tile.getStyle()
                .set("transform", "translateY(-3px)").set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("border-color", "var(--lumo-primary-color-50pct)"));
        tile.getElement().addEventListener("mouseleave", event -> tile.getStyle()
                .remove("transform").set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("border-color", "var(--lumo-contrast-10pct)"));
        tile.getElement().addEventListener("focus", event -> tile.getStyle()
                .set("outline", "2px solid var(--lumo-primary-color)").set("outline-offset", "2px"));
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
