package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.session.AccessDecision;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.session.UserDTO;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.security.service.UserPermissionService;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserAccountsTabLayout extends SettingPageLayout {

    private final UserPermissionService userPermissionService;

    public UserAccountsTabLayout(UserProxyService userProxyService,
            UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
        Grid<ShortUserDTO> userGrid = new Grid<>();
        userGrid.addColumn(ShortUserDTO::id).setHeader("ID").setSortable(true);
        userGrid.addColumn(ShortUserDTO::name).setHeader("Login").setSortable(true);
        userGrid.addColumn(new ComponentRenderer<>(Button::new, this::createPermissionButton))
                .setHeader("Permissions").setSortable(false);
        userGrid.setSizeFull();
        userGrid.setItems(userProxyService.getAllUsers());
        userGrid.setSizeFull();

        add(userGrid);
        setSizeFull();
    }

    void createPermissionButton(Button button, ShortUserDTO user) {
        if (user.id() == UserDTO.ADMIN_ID) {
            button.setText("ALL PERMISSIONS");
            button.setEnabled(false);
        } else {
            button.setText("Permissions");
            button.setIcon(VaadinIcon.EDIT.create());
            button.addClickListener(e -> modifyUserPermissionsEvent(user));
        }
    }

    void modifyUserPermissionsEvent(ShortUserDTO user) {
        Dialog addDialog = new Dialog();
        addDialog.setResizable(true);

        addDialog.setWidth(700, Unit.PIXELS);
        addDialog.setHeight(600, Unit.PIXELS);
        addDialog.setCloseOnOutsideClick(false);
        addDialog.setHeaderTitle("User " + user.name() + " permissions");

        Grid<PermissionDetailConfig> permissionGrid = new Grid<>();
        permissionGrid.addColumn(PermissionDetailConfig::getCategory)
                .setHeader("Category")
                .setSortable(true)
                .setResizable(true);
        permissionGrid.addColumn(PermissionDetailConfig::getKey).setHeader("Key").setSortable(true).setResizable(true);
        permissionGrid.addColumn(PermissionDetailConfig::getLabel)
                .setHeader("Label")
                .setSortable(true)
                .setResizable(true);
        permissionGrid.addColumn(PermissionDetailConfig::getDescription).setHeader("Description").setResizable(true);
        permissionGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (checkbox, perm) -> {
                    checkbox.setValue(perm.getDecision().isAllowed());
                    checkbox.addValueChangeListener(
                            e -> modifyUserPermissionEvent(user, perm, e.getValue()));
                })).
                setHeader("Description").setSortable(true);
        permissionGrid.setSizeFull();

        permissionGrid.setItems(userPermissionService.getUserPermissionsWithDetails(user.id()));
        addDialog.add(permissionGrid);
        addDialog.open();

    }

    private void modifyUserPermissionEvent(ShortUserDTO user, PermissionDetailConfig perm, boolean allowed) {
        userPermissionService.updateUserAction(user.id(), perm, allowed ? AccessDecision.ALLOW : AccessDecision.DENY);
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }
}
