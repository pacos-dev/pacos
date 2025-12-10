package org.pacos.base.utils.component;

import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ButtonGroupUtilsTest {

    @Test
    void whenCreateThenNoException() {
        Assertions.assertDoesNotThrow(() -> new ButtonGroupUtils(new Button()).floatRight());
    }

}