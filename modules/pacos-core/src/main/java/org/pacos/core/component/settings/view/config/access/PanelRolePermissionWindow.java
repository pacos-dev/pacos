package org.pacos.core.component.settings.view.config.access;

import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.security.dto.AppPermissionConfig;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.PermissionService;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class PanelRolePermissionWindow extends DesktopWindow {

    private final RoleDTO roleDTO;
    private final PermissionService permissionService;

    protected PanelRolePermissionWindow(RolePermissionWindowConfig moduleConfig,
                                        RoleDTO roleDTO,
                                        PermissionService permissionService) {
        super(moduleConfig);
        this.roleDTO = roleDTO;
        this.permissionService = permissionService;

        Grid<AppPermissionConfig> permissionGrid = new Grid<>(AppPermissionConfig.class, false);
        permissionGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (checkbox, perm) -> {
                    checkbox.setValue(perm.isActive());
                    checkbox.addValueChangeListener(
                            e -> modifyUserPermissionEvent(perm, e.getValue()));
                })).
                setHeader("Disable/Enable").setWidth("50px");
        permissionGrid.addColumn(AppPermissionConfig::getCategory)
                .setHeader("Category")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        permissionGrid.addColumn(AppPermissionConfig::getKey)
                .setHeader("Key")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        permissionGrid.addColumn(AppPermissionConfig::getLabel)
                .setHeader("Label")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        permissionGrid.addColumn(AppPermissionConfig::getDescription)
                .setHeader("Description")
                .setResizable(true)
                .setAutoWidth(true);

        permissionGrid.setSizeFull();
        permissionGrid.setItems(permissionService.loadPermissionsConfig(roleDTO));
        add(permissionGrid);
    }

    private void modifyUserPermissionEvent(AppPermissionConfig perm, Boolean value) {
        permissionService.savePermissionState(perm.getId(), value, roleDTO.getId());
    }

    public RoleDTO getRoleDTO() {
        return roleDTO;
    }
}
