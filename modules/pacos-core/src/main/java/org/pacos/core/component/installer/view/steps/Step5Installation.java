package org.pacos.core.component.installer.view.steps;

import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/**
 * Displays installation progress and automatically completes the core installation.
 */
@SuppressWarnings("squid:S110")
public class Step5Installation extends Box {

    private boolean installationStarted;

    public Step5Installation(InstallerView installerView) {
        super(installerView, "Pac OS - Installation");
        setProgressBarValue(0.6);
        BoxContent content = new BoxContent();
        add(content);
        content.addTextLine("Installation in progress. Once completed, you will be forwarded to home page");
        content.add(new HtmlComponent("my-spinner"));

        addAttachListener(event -> {
            if (!installationStarted) {
                installationStarted = true;
                completeInstallation(event.getUI());
            }
        });
    }

    private void completeInstallation(UI ui) {
        try {
            installerView.getInstallerService().configure(
                    installerView.getSettings(),
                    progress -> setProgressBarValue(progress));
            closeSession();
            ui.navigate("");
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
            throw new IllegalStateException("Unable to complete PacOS installation", e);
        }
    }

    private void closeSession() {
        VaadinSession.getCurrent().setAttribute(UserSession.class, null);
        VaadinSession.getCurrent().close();
    }

    @Override
    protected void nextBtnEvent() {
        // No next button is available during installation.
    }

    @Override
    protected void backBtnEvent() {
        // No back button is available during installation.
    }
}
