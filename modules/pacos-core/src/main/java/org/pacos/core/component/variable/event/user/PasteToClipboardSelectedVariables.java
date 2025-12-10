package org.pacos.core.component.variable.event.user;

import java.util.Set;

import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.view.param.ParamParser;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.system.view.PacosJS;

public final class PasteToClipboardSelectedVariables {
    private PasteToClipboardSelectedVariables() {
    }

    public static void fireEvent(Set<UserVariableDTO> selected) {
        if (selected.isEmpty()) {
            return;
        }
        String value = ParamParser.mapToString(selected.stream().map(UserVariableDTO::toParam).toList());
        NotificationUtils.success("Copied " + selected.size() + " variables to Clipboard");
        PacosJS.pasteToClipboard(value);
    }
}
