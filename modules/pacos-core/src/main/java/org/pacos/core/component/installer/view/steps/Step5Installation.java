package org.pacos.core.component.installer.view.steps;

import java.util.concurrent.CompletableFuture;

import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/**
 * Displays installation progress and automatically completes the core installation.
 */
@SuppressWarnings("squid:S110")
public class Step5Installation extends Box {

    private static final Logger LOG = LoggerFactory.getLogger(Step5Installation.class);

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
                startInstallation(event.getUI());
            }
        });
    }

    private void startInstallation(UI ui) {
        CompletableFuture.runAsync(() -> configureInstallation(ui));
    }

    private void configureInstallation(UI ui) {
        try {
            installerView.getInstallerService().configure(
                    installerView.getSettings(),
                    progress -> ui.access(() -> setProgressBarValue(progress)));
            ui.access(() -> finishInstallation(ui));
        } catch (Exception e) {
            LOG.error("Unable to complete PacOS installation", e);
            ui.access(() -> NotificationUtils.error(e.getMessage()));
        }
    }

    private void finishInstallation(UI ui) {
        VaadinSession session = ui.getSession();
        session.setAttribute(UserSession.class, null);
        session.close();
        ui.getPage().setLocation("");
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
