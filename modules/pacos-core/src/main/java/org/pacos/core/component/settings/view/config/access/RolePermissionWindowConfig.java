package org.pacos.core.component.settings.view.config.access;

import java.util.Objects;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.security.dto.RoleDTO;

public class RolePermissionWindowConfig implements WindowConfig {

    private final RoleDTO roleDTO;

    public RolePermissionWindowConfig(RoleDTO roleDTO) {
        this.roleDTO = roleDTO;
    }

    @Override
    public String title() {
        return "Permissions for role " + roleDTO.getLabel();
    }

    @Override
    public String icon() {
        return PacosIcon.UNLOCK.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return PanelRolePermissionWindow.class;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RolePermissionWindowConfig that = (RolePermissionWindowConfig) o;
        return Objects.equals(roleDTO, that.roleDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleDTO);
    }
}
