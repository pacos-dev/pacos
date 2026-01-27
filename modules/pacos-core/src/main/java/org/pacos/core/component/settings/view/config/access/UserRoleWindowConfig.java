package org.pacos.core.component.settings.view.config.access;

import java.util.Objects;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;

public class UserRoleWindowConfig implements WindowConfig {

    private final ShortUserDTO shortUserDTO;

    public UserRoleWindowConfig(ShortUserDTO shortUserDTO) {
        this.shortUserDTO = shortUserDTO;
    }

    @Override
    public String title() {
        return "Roles for user " + shortUserDTO.name();
    }

    @Override
    public String icon() {
        return PacosIcon.UNLOCK.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return UserRoleWindowLayout.class;
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
        UserRoleWindowConfig that = (UserRoleWindowConfig) o;
        return Objects.equals(shortUserDTO, that.shortUserDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shortUserDTO);
    }

    public ShortUserDTO getShortUserDTO() {
        return shortUserDTO;
    }
}
