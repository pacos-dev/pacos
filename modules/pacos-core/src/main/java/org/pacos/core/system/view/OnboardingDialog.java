package org.pacos.core.system.view;

import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Presents the first-run guide to the user who owns the initial PacOS setup.
 * The dismissal state is stored globally and can only be changed by that owner.
 */
public class OnboardingDialog extends Dialog {

    private final RegistryProxy registryProxy;

    public OnboardingDialog(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
        setHeaderTitle("Getting started with PacOS");
        setWidth("620px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.add(new Paragraph("Your PacOS environment is ready. Use this guide to explore the main areas. "
                + "You can follow the topics in any order."));
        content.add(section("Applications", "Open applications from the Applications menu and arrange your workspace."));
        content.add(section("User settings", "Configure your account and personal preferences."));
        content.add(section("Appearance", "Switch between the light and dark interface themes."));
        content.add(section("App Center", "Discover and install extensions after the core system is installed."));
        content.add(section("Variables", "Manage user variables used by PacOS tools and extensions."));
        content.add(new Anchor("https://pacos.dev", "PacOS website"));
        add(content);

        Button continueButton = new Button("Continue to PacOS", event -> close());
        Button skipButton = new Button("Skip guide", event -> confirmDismissal());
        getFooter().add(skipButton, continueButton);
    }

    private Paragraph section(String title, String description) {
        Paragraph paragraph = new Paragraph(title + " — " + description);
        paragraph.getElement().getStyle().set("margin", "0");
        return paragraph;
    }

    private void confirmDismissal() {
        Dialog confirmation = new Dialog();
        confirmation.setHeaderTitle("Skip the getting started guide?");
        confirmation.add(new Paragraph("You can return to this guide later from the user panel."));
        Button cancel = new Button("Keep guide", event -> confirmation.close());
        Button confirm = new Button("Skip guide", event -> {
            registryProxy.saveRegistry(RegistryName.ONBOARDING_DISMISSED, true);
            confirmation.close();
            close();
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
        int ownerId = registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, -1);
        if (ownerId != session.getUserId()
                || registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false)) {
            return;
        }
        new OnboardingDialog(registryProxy).open();
    }
}
