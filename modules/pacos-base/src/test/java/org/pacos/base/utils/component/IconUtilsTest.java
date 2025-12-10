package org.pacos.base.utils.component;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IconUtilsTest {

    @Test
    void whenColorIconThenSetCssStyle() {
        Icon icon = IconUtils.colorIcon(() -> new Icon(VaadinIcon.USER), Color.GREEN);

        Assertions.assertEquals(Color.GREEN.getColor(), icon.getElement().getAttribute(Color.GREEN.name()));
    }

    @Test
    void whenTetiaryIconThenSetVariableStyle() {
        Icon icon = IconUtils.tetiaryIcon(VaadinIcon.USER);
        assertEquals("var(--lumo-tertiary-text-color)", icon.getColor());
    }

}
