package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.pacos.core.component.installer.view.steps.helper.BoxMode;

@SuppressWarnings("squid:S110")
public class Step2ChooseInstallationMode extends Box {


    private static final String ACTIVE = "active";
    private final BoxMode singleMode;
    private final BoxMode multiMode;
    private final Button nextButton;
    private final transient InstallerSettings settings;

    public Step2ChooseInstallationMode(InstallerView installerView) {
        super(installerView, "Pac OS - Installation mode");
        setProgressBarValue(0.1);
        this.settings = installerView.getSettings();
        BoxContent content = new BoxContent();
        content.addTextLine("Select one of the available installation modes");

        HorizontalLayout modeLayout = new HorizontalLayout();
        modeLayout.setWidthFull();

        this.singleMode = new BoxMode();
        singleMode.setImage("icons/user.png");
        singleMode.addTitle("Personal");
        singleMode.addListItem("- Local machine");
        singleMode.addListItem("- Automatic access");
        singleMode.addListItem("- No user management");
        singleMode.addClickListener(e -> setModeEvent(InstallationMode.SINGLE));

        this.multiMode = new BoxMode();
        multiMode.setImage("icons/user_group.png");
        multiMode.addTitle("Group");
        multiMode.addListItem("- Server machine");
        multiMode.addListItem("- User management");
        multiMode.addListItem("- Login panel");
        multiMode.addClickListener(e -> setModeEvent(InstallationMode.MULTI));

        modeLayout.add(singleMode);
        modeLayout.add(multiMode);
        content.add(modeLayout);
        add(content);

        addBackBtn();

        this.nextButton = addNextBtn();

        this.nextButton.setVisible(settings.getInstallationMode() != null);
        this.setActiveElement(settings.getInstallationMode());
    }

    @Override
    protected void nextBtnEvent() {
        if (settings.getInstallationMode().isSingle()) {
            installerView.setView(new Step4AdditionalSettings(installerView));
        } else {
            installerView.setView(new Step3SetAdminPassword(installerView));
        }

    }

    @Override
    protected void backBtnEvent() {
        installerView.setView(new Step1Welcome(installerView));
    }

    protected void setModeEvent(InstallationMode mode) {
        this.nextButton.setVisible(true);
        this.settings.setInstallationMode(mode);
        singleMode.removeClassName(ACTIVE);
        multiMode.removeClassName(ACTIVE);
        setActiveElement(mode);
    }

    private void setActiveElement(InstallationMode mode) {
        if (mode != null) {
            if (mode.isSingle()) {
                singleMode.addClassName(ACTIVE);
            } else {
                multiMode.addClassName(ACTIVE);
            }
        }
    }

}
