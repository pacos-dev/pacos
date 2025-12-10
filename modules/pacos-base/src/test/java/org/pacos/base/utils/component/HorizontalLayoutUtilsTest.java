package org.pacos.base.utils.component;

import com.vaadin.flow.component.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HorizontalLayoutUtilsTest {

    @Test
    void whenDefaultsIsCalledThenLayoutHasCorrectDefaultProperties() {
        HorizontalLayoutUtils layout = HorizontalLayoutUtils.defaults();

        assertEquals("100%", layout.getWidth());
        assertEquals("100%", layout.getHeight());
        assertFalse(layout.isPadding());
        assertFalse(layout.isSpacing());
        assertFalse(layout.isMargin());
    }

    @Test
    void whenWithHeightIsCalledThenLayoutHeightIsSetCorrectly() {
        HorizontalLayoutUtils layout = new HorizontalLayoutUtils();

        layout.withHeight(200, Unit.PIXELS);

        assertEquals("200.0px", layout.getHeight());
    }
}
