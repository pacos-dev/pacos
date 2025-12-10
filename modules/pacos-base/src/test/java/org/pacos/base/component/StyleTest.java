package org.pacos.base.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StyleTest {

    @Test
    void whenToStringThenReturnStylName() {
        assertEquals("margin-top", Style.MARGIN_TOP.toString());
        assertEquals("margin-top", Style.MARGIN_TOP.value());
    }

}