package org.pacos.core.component.ai.view.tab;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.window.shortcut.ShortcutType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.textfield.TextField;

@Component
@Scope("prototype")
public class AISettingsTab extends SettingPageLayout {

    public AISettingsTab() {
        TextField textField = new TextField("LLM Api", "http://localhost:");
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }

}
