package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.security.service.PermissionDefaultService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DefaultPermissionsTabLayout extends SettingPageLayout {

    public DefaultPermissionsTabLayout(PermissionDefaultService defaultService) {
        Grid<PermissionDetailConfig> permissionGrid = new Grid<>();
        permissionGrid.addColumn(PermissionDetailConfig::getCategory).setHeader("Category").setSortable(true).setResizable(true);
        permissionGrid.addColumn(PermissionDetailConfig::getKey).setHeader("Key").setSortable(true).setResizable(true);
        permissionGrid.addColumn(PermissionDetailConfig::getLabel).setHeader("Label").setSortable(true).setResizable(true);
        permissionGrid.addColumn(PermissionDetailConfig::getDescription).setHeader("Description").setResizable(true);

        permissionGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (checkbox, permissionConfig) -> {
            checkbox.setValue(permissionConfig.getDecision().isAllowed());
            checkbox.addValueChangeListener(e ->
                    updateConfiguration(defaultService, permissionConfig, e));
        })).setHeader("Allowed").setSortable(true);
        permissionGrid.setSizeFull();

        add(new InfoBox("The default configuration is applied to all new accounts that are added to the system."
                + "If the user does not have full permission configuration, the missing keys will be retrieved from "
                + "the default configuration"));
        add(permissionGrid);

        setSizeFull();
        addAttachListener(attachEvent -> permissionGrid.setItems(defaultService.findAllOrdered()));
    }

    private static void updateConfiguration(PermissionDefaultService defaultService, PermissionDetailConfig permissionConfig,
            AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> e) {
        defaultService.updateConfiguration(permissionConfig, e.getValue());
        NotificationUtils.success("Configuration was updated");
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }
}
