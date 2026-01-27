package org.pacos.core.component.settings.view.config.access;

import java.util.Set;

import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.common.event.OnSaveEvent;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.user.proxy.UserProxyService;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;

public class UserRoleWindowLayout extends DesktopWindow {

    private final ShortUserDTO user;
    private final UserProxyService userProxyService;
    private final Set<RoleDTO> userRoleIds;
    private final OnSaveEvent<ShortUserDTO> onSaveEvent;

    protected UserRoleWindowLayout(UserRoleWindowConfig moduleConfig, UserProxyService userProxyService, OnSaveEvent<ShortUserDTO> onSaveEvent) {
        super(moduleConfig);
        this.setSize(300, 200);
        this.user = moduleConfig.getShortUserDTO();
        this.userProxyService = userProxyService;
        this.userRoleIds = userProxyService.getUserService().loadRoles(user.id());
        this.onSaveEvent = onSaveEvent;
        Grid<RoleDTO> roleDTOGrid = new Grid<>();
        roleDTOGrid.addComponentColumn(this::createCheckBoxBtn).setWidth("30px");
        roleDTOGrid.addColumn(RoleDTO::getLabel).setHeader("Label").setAutoWidth(true).setResizable(true);
        roleDTOGrid.addColumn(RoleDTO::getDescription).setHeader("Description").setAutoWidth(true).setResizable(true);
        add(roleDTOGrid);
        addAttachListener(e -> roleDTOGrid.setItems(userProxyService.getRoleService().loadAllRoles()));
    }

    private Checkbox createCheckBoxBtn(RoleDTO roleDTO) {
        Checkbox cb = new Checkbox();
        cb.setValue(userRoleIds.stream().anyMatch(e -> e.getId().equals(roleDTO.getId())));
        cb.addValueChangeListener(e ->
        {
            if (e.getValue()) {
                userRoleIds.add(roleDTO);
            } else {
                userRoleIds.remove(roleDTO);
            }
            updateUserRoles();
        });
        return cb;
    }

    private void updateUserRoles() {
        userProxyService.getUserService().setRoles(userRoleIds, user.id());
        NotificationUtils.success("User roles updated");
        onSaveEvent.onSaveEvent(user);
    }
}
