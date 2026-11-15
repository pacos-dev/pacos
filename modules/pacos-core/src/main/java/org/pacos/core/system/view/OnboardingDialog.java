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
        content.add(new Paragraph("Applications — launch tools from the Applications menu and arrange your workspace."));
        content.add(action("Open user settings", () -> showWindow(UserConfig.class)));
        content.add(action("Open App Center", () -> showWindow(AppStoreWindowConfig.class)));
        content.add(new Paragraph("Appearance — change the interface theme using the appearance control in the user panel."));
        content.add(new Paragraph("Variables — use the {v} toolbar shortcut to manage your user variables."));
        content.add(new Anchor("https://pacos.dev/docs/user", "Documentation"));
        content.add(new Anchor("https://pacos.dev", "PacOS website"));
        add(content);

        Button continueButton = new Button("Continue to PacOS", event -> close());
        Button skipButton = new Button("Skip guide", event -> confirmDismissal());
        getFooter().add(skipButton, continueButton);
    }

    private Button action(String caption, Runnable action) {
        Button button = new Button(caption, event -> action.run());
        button.setWidthFull();
        return button;
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
                || registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false)) {
            return;
        }
        new OnboardingDialog(registryProxy).open();
    }
}
