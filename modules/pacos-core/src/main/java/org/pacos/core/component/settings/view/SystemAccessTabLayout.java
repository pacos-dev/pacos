package org.pacos.core.component.settings.view;

import com.vaadin.flow.component.html.Hr;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.CheckboxUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SystemAccessTabLayout extends SettingPageLayout {

    private final transient RegistryProxy registryProxy;

    public SystemAccessTabLayout(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
        add(new InfoBox("System access"));

        boolean guestModeEnabled = registryProxy.isGuestMode();
        boolean registrationEnabled = registryProxy.isRegistrationPanelEnabled();

        CheckboxUtils guestModeCheckBox =
                new CheckboxUtils("Guest mode enabled - allow access without having to log in");
        guestModeCheckBox.withEnabledForPermission(SystemPermissions.SYSTEM_ACCESS_CONFIGURATION);
        guestModeCheckBox.setValue(guestModeEnabled);
        guestModeCheckBox
                .addValueChangeListener(e -> saveRegistryValue(e.getValue(), RegistryName.GUEST_MODE, "guest mode"));

        CheckboxUtils registrationEnabledCheckBox = new CheckboxUtils("Enable registration panel for new account");
        registrationEnabledCheckBox.withEnabledForPermission(SystemPermissions.SYSTEM_ACCESS_CONFIGURATION);
        registrationEnabledCheckBox.setValue(registrationEnabled);
        registrationEnabledCheckBox
                .addValueChangeListener(e -> saveRegistryValue(e.getValue(),
                        RegistryName.REGISTRATION_PANEL,
                        "registration panel"));

        add(guestModeCheckBox);
        add(registrationEnabledCheckBox);
        add(new Hr());

    }

    void saveRegistryValue(boolean value, RegistryName registryName, String infoMsh) {
        registryProxy.saveRegistry(registryName, value);
        String result = value ? "Enable " : "Disable ";
        NotificationUtils.success(result + infoMsh);
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //shortcut listener not implemented
    }
}