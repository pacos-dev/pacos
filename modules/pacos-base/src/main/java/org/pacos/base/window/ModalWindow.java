package org.pacos.base.window;

import org.pacos.base.component.Theme;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.utils.component.HorizontalLayoutUtils;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.window.config.impl.ModalWindowConfig;

/**
 * Basic window implementation not managed by spring. Configuration is provided directly through the constructor.
 */
public class ModalWindow extends DesktopWindow {

    public ModalWindow(ModalWindowConfig moduleConfig) {
        super(moduleConfig);
        if (moduleConfig.isWarning()) {
            this.addThemeName(Theme.BLACK_WINDOW.getName());
        }
        this.removeThemeName("app-dialog");
        moduleConfig.getContent().setPadding(true);
        allowCloseOnEsc();
        moduleConfig.getWindowState().apply(this);
        add(moduleConfig.getContent());
        if (moduleConfig.isWarning()) {
            add(HorizontalLayoutUtils.defaults(
                    new ImageUtils(PacosIcon.ALERT.getUrl(), "alert")
                            .withWidth(60)
                            .withHeight(60)
                            .withStyle("margin", "10px 15px 15px 25px")
                            .withStyle("user-select", "none"),
                    moduleConfig.getContent()));
        } else {
            add(moduleConfig.getContent());
        }
    }
}
