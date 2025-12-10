package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.server.VaadinSession;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.pacos.core.component.session.service.ServiceListener;

@SuppressWarnings("squid:S110")
public class Step1Welcome extends Box {

    public Step1Welcome(InstallerView installerView) {
        super(installerView, "Pac OS - Welcome");

        UserSession session = UserSession.getCurrent();
        if (UserSession.getCurrent() == null) {
            session = new UserSession(null);
            VaadinSession.getCurrent().setAttribute(UserSession.class, session);
        }
        session.setUiSystem(new UISystem(null, null, null, null, null, null));
        ServiceListener.addVaadinSession(VaadinSession.getCurrent());
        BoxContent content = new BoxContent();
        content.addTextLine("Thank you for choosing the pacos.");
        content.addTextLine("Go through all installation steps to configure the pacos according to your requirements.");
        add(content);

        addNextBtn();
    }

    @Override
    protected void nextBtnEvent() {
        installerView.setView(new Step2ChooseInstallationMode(installerView));
    }

    @Override
    protected void backBtnEvent() {
        //no back btn available
    }
}
