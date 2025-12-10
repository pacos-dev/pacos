package org.pacos.base.utils.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;

public class ButtonGroupUtils extends Div {

    public ButtonGroupUtils(Button... buttons) {
        for (Button btn : buttons) {
            add(btn);
        }
        addClassName("btn-group");
    }

    public ButtonGroupUtils floatRight() {
        addClassName("right");
        return this;
    }
}
