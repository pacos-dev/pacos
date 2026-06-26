package org.pacos.core.system.event;

import org.pacos.base.event.UISystem;
import org.pacos.core.component.ai.view.ChatWindow;

public final class OpenAIChatEvent {
    private OpenAIChatEvent() {
    }

    public static void fireEvent(UISystem system, ChatWindow chatWindow) {
        chatWindow.open();
    }
}
