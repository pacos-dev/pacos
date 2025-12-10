package org.pacos.common.view.param.event;

import java.util.Set;

import com.vaadin.flow.component.UI;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.view.param.Param;
import org.pacos.common.view.param.ParamParser;

public final class PasteToClipboardSelectedParam {

    private PasteToClipboardSelectedParam() {
    }

    public static void fireEvent(Set<Param> selected) {
        if (selected.isEmpty()) {
            return;
        }
        String value = ParamParser.mapToString(selected.stream().toList());
        NotificationUtils.success("Copied " + selected.size() + " variables to Clipboard");
        UI.getCurrent().getPage().executeJs(
                """
                        navigator.clipboard.writeText($0)
                        .catch(err => console.error('Error writing text to clipboard', err));
                        """,
                value
        );
    }
}