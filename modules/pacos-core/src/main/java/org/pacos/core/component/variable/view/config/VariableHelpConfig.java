package org.pacos.core.component.variable.view.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.variable.view.help.PanelVariableHelp;
import org.springframework.stereotype.Component;

@Component
public class VariableHelpConfig implements WindowConfig {

    @Override
    public String title() {
        return "Variable help";
    }

    @Override
    public String icon() {
        return PacosIcon.QUESTION.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return PanelVariableHelp.class;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return false;
    }

}
