package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S110")
public class Step7Installation extends Box {

    private static final Logger LOG = LoggerFactory.getLogger(Step7Installation.class);

    public Step7Installation(InstallerView installerView) {
        super(installerView, "Pac OS - Installation");
        setProgressBarValue(0.6);
        BoxContent content = new BoxContent();
        add(content);

        content.addTextLine("Installation in progress. Once completed, you will be forwarded to home page");
        content.add(new HtmlComponent("my-spinner"));


        UI ui = UI.getCurrent();
        try {
            installerView.getInstallerService().configure(installerView.getSettings(), progress -> ui.access(() -> setProgressBarValue(progress)));
        } catch (Exception e) {
            LOG.error("Error while installing system", e);
            NotificationUtils.error(e.getMessage());
            throw e;
        }
        closeSession();

        UI.getCurrent().navigate("");
    }

    void closeSession() {
        VaadinSession.getCurrent().setAttribute(UserSession.class, null);
        VaadinSession.getCurrent().close();
    }


    @Override
    protected void nextBtnEvent() {
        //no next btn available
    }

    @Override
    protected void backBtnEvent() {
        //no back btn available
    }
}
