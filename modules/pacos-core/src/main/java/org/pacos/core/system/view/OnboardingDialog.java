package org.pacos.core.system.view;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.user.view.config.UserConfig;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        setWidth("660px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.add(new Paragraph("Your PacOS environment is ready. Explore the areas below in any order. "
                + "The guide does not change your configuration."));
        content.add(new Paragraph("Work through these topics in any order. Tick them as you explore PacOS."));
        addChecklistItem(content, "Explore applications",
                "Open Applications from the desktop toolbar to discover the tools available in your installation.");
        addChecklistItem(content, "Configure your account",
                "Review your account details and personal settings.",
                "Open user settings", () -> showWindow(UserConfig.class));
        addChecklistItem(content, "Install extensions",
                "Browse available extensions and install the tools required by your team.",
                "Open App Center", () -> showWindow(AppStoreWindowConfig.class));
        addChecklistItem(content, "Choose your appearance",
                "Switch between light and dark themes using the appearance control in the user panel.");
        addChecklistItem(content, "Review user variables",
                "Use the {v} toolbar shortcut to manage variables available to your user.");
        content.add(new Anchor("https://pacos.dev/docs/user", "Documentation"));
        content.add(new Anchor("https://pacos.dev", "PacOS website"));
        add(content);

        Button closeButton = new Button("Close guide", event -> confirmDismissal());
        getFooter().add(closeButton);
    }

    private void addChecklistItem(VerticalLayout parent, String title, String description) {
        addChecklistItem(parent, title, description, null, null);
    }

    private void addChecklistItem(VerticalLayout parent, String title, String description,
                                  String actionLabel, Runnable action) {
        Checkbox checkbox = new Checkbox(title);
        Paragraph details = new Paragraph(description);
        details.getStyle().set("margin", "0");
        VerticalLayout text = new VerticalLayout(checkbox, details);
        text.setPadding(false);
        text.setSpacing(false);

        HorizontalLayout item = new HorizontalLayout(text);
        item.setWidthFull();
        if (actionLabel != null) {
            Button button = new Button(actionLabel, event -> action.run());
            item.add(button);
        }
        parent.add(item);
    }

    private void showWindow(Class<? extends org.pacos.base.window.config.WindowConfig> config) {
        UISystem system = UISystem.getCurrent();
        if (system != null && system.getWindowManager() != null) {
            system.getWindowManager().showWindow(config);
        }
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
        Integer ownerId = registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null);
        if (ownerId == null || ownerId != session.getUserId()
                || Boolean.TRUE.equals(registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false))) {
            return;
        }
        new OnboardingDialog(registryProxy).open();
    }
}
