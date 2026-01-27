package org.pacos.core.component.settings.view.config.access;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.session.UserDTO;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;

@Component
@Scope("prototype")
public class UserAccountsTabLayout extends SettingPageLayout {

    private static final String ID = "ID";
    private static final String LOGIN = "Login";
    private static final String ROLES = "Roles";
    private final UserProxyService userProxyService;
    private final Grid<ShortUserDTO> userGrid;

    public UserAccountsTabLayout(UserProxyService userProxyService) {
        this.userProxyService = userProxyService;
        this.userGrid = new Grid<>();
        userGrid.addColumn(ShortUserDTO::id).setHeader(ID).setSortable(true);
        userGrid.addColumn(ShortUserDTO::name).setHeader(LOGIN).setSortable(true);
        userGrid.addColumn(ShortUserDTO::getRoleString).setHeader(ROLES).setSortable(true);
        userGrid.addColumn(new ComponentRenderer<>(Button::new, this::createEditRolesButton))
                .setHeader(ROLES).setSortable(false);
        userGrid.setSizeFull();

        addAttachListener(e -> refreshGridItems(userProxyService));
        userGrid.setSizeFull();

        add(userGrid);
        setSizeFull();
    }

    private void refreshGridItems(UserProxyService userProxyService) {
        userGrid.setItems(userProxyService.getAllUsers());
    }

    public static String getSearchIndex() {
        return ID + LOGIN + ROLES;
    }

    void createEditRolesButton(Button button, ShortUserDTO user) {
        button.setText(ROLES);
        button.setIcon(VaadinIcon.EDIT.create());
        if (user.id() == UserDTO.ADMIN_ID) {
            button.setEnabled(false);
        } else {
            button.addClickListener(e -> modifyUserPermissionsEvent(user));
        }
    }

    void modifyUserPermissionsEvent(ShortUserDTO user) {
        UserRoleWindowConfig userRoleWindowConfig = new UserRoleWindowConfig(user);
        UserRoleWindowLayout window = new UserRoleWindowLayout(userRoleWindowConfig, userProxyService, updatedUser -> refreshGridItems(userProxyService));
        UISystem.getCurrent().getWindowManager().showWindow(window);
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }
}
