package org.pacos.base.utils.component;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import org.pacos.base.component.Theme;

@SuppressWarnings("squid:S110")
public class TextAreaUtils extends TextArea {

    public TextAreaUtils() {
        addThemeVariants(TextAreaVariant.LUMO_SMALL);
        addThemeName(Theme.NO_BORDER.getName());
        setWidthFull();
    }

    public TextAreaUtils(String label) {
        this();
        setLabel(label);
    }
}
