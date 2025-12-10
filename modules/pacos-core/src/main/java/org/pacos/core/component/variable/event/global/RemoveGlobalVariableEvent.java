package org.pacos.core.component.variable.event.global;

import com.vaadin.flow.component.html.Span;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.event.OnConfirmEvent;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public final class RemoveGlobalVariableEvent {

    private RemoveGlobalVariableEvent() {
    }

    public static void fireEvent(GlobalVariableSystem system) {
        SystemVariableDTO selectedValue = system.getVariableGrid()
                .asSingleSelect()
                .getValue();
        if (selectedValue == null) {
            return;
        }

        final ConfirmationWindowConfig config = new ConfirmationWindowConfig(onConfirmEvent(system, selectedValue));
        config.setContent(VerticalLayoutUtils.defaults(
                new Span("Are you sure you want to remove '" + selectedValue.getName() + "' variable?")
        ));
        UISystem.getCurrent().getWindowManager().showModalWindow(config);
    }

    static OnConfirmEvent onConfirmEvent(GlobalVariableSystem system, SystemVariableDTO dto) {
        return () -> {
            system.getSystemVariableProxy().removeVariable(dto);
            system.notify(GlobalVariableEvent.REMOVED_ENTRY, dto);
            return true;
        };
    }
}
