package org.pacos.core.component.variable.view.global;

import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.component.SplitterUtils;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public class SystemVariableSettings extends SettingPageLayout {

    private static final String INFO = "System variables can be used in any field that supports variables. " +
            "The system variable value can be overwritten by the other module/application or by user variables. " +
            "System variables use the javascript engine and therefore enable the handling of " +
            "dynamically generated values.";

    private final SystemVariableTabSheet tabSheet;

    public SystemVariableSettings(SystemVariableProxy globalVariableProxy) {
        setSizeFull();
        GlobalVariableSystem system = new GlobalVariableSystem(globalVariableProxy);

        SystemVariableGrid variableGrid = new SystemVariableGrid(system);
        VariableMenuBar menu = new VariableMenuBar(system);
        this.tabSheet = new SystemVariableTabSheet(system);

        InfoBox infoBox = new InfoBox(INFO);
        add(infoBox, VerticalLayoutUtils.defaults(menu, new SplitterUtils(variableGrid,
                tabSheet,
                30).orientation(SplitLayout.Orientation.HORIZONTAL)));
    }

    public static String getSearchIndex() {
        return INFO;
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        if (shortcutType.isSave()) {
            tabSheet.save();
        }
    }
}
