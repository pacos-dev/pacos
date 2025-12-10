package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import org.pacos.core.component.installer.settings.AdditionalSettings;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;

@SuppressWarnings("squid:S110")
public class Step4AdditionalSettings extends Box {


    private final Binder<AdditionalSettings> binder;

    public Step4AdditionalSettings(InstallerView installerView) {
        super(installerView, "Pac OS - Configuration");
        setProgressBarValue(0.3);
        AdditionalSettings settings = installerView.getSettings().getAdditionalSettings();
        BoxContent content = new BoxContent();
        add(content);

        this.binder = new Binder<>();
        binder.setBean(settings);


        VerticalLayout vl = new VerticalLayout();
        content.add(vl);

        if (installerView.getSettings().getInstallationMode().isMulti()) {
            vl.add("Additional access");
            Checkbox registration = new Checkbox();
            setMargin(registration);
            registration.setLabel("Enable registration panel for new users");
            vl.add(registration);

            Checkbox guestAccount = new Checkbox();
            setMargin(guestAccount);
            guestAccount.setLabel("Enable guest account");
            vl.add(guestAccount);

            binder.forField(registration)
                    .bind(AdditionalSettings::isRegistrationEnabled, AdditionalSettings::setRegistrationEnabled);
            binder.forField(guestAccount)
                    .bind(AdditionalSettings::isGuestAccountEnabled, AdditionalSettings::setGuestAccountEnabled);
        }
        vl.add("Additional permission");
        Checkbox permission = new Checkbox();
        setMargin(permission);
        permission.setLabel("Allow to automatically sends logs about the application error");
        vl.add(permission);

        binder.forField(permission)
                .bind(AdditionalSettings::isSendingErrorLogAllowed, AdditionalSettings::setSendingErrorLogAllowed);

        vl.add("Auto update");
        Checkbox autoUpdate = new Checkbox();
        setMargin(autoUpdate);
        autoUpdate.setLabel("Enable automatic update - applies to both the system and extensions. " +
                "The system will check for updates and restart the system after they are performed.");
        vl.add(autoUpdate);

        binder.forField(autoUpdate)
                .bind(AdditionalSettings::isAutoUpdateEnabled, AdditionalSettings::setAutoUpdateEnabled);


        addBackBtn();
        addNextBtn();
    }

    @Override
    protected void nextBtnEvent() {
        binder.writeBeanIfValid(installerView.getSettings().getAdditionalSettings());
        installerView.setView(new Step5CheckingResources(installerView));

    }

    @Override
    protected void backBtnEvent() {
        binder.writeBeanIfValid(installerView.getSettings().getAdditionalSettings());
        if (installerView.getSettings().getInstallationMode().isSingle()) {
            installerView.setView(new Step2ChooseInstallationMode(installerView));
        } else {
            installerView.setView(new Step3SetAdminPassword(installerView));
        }
    }

    private void setMargin(Checkbox checkbox) {
        checkbox.getStyle().set("margin-left", "20px");
    }

}
