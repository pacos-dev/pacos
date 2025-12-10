package org.pacos.core.component.variable.event.global;

import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public final class SaveGlobalVariableEvent {

    private SaveGlobalVariableEvent() {
    }

    public static void fireEvent(TabMonitoring tab, SystemVariableDTO variableDto, GlobalVariableSystem system) {
        system.getSystemVariableProxy().save(variableDto);
        tab.updateLabel(variableDto.getName());
        system.notify(GlobalVariableEvent.REFRESH_ENTRY, variableDto);
        NotificationUtils.success("Saved");
    }
}
