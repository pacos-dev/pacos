package org.pacos.base.component.icon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IconClassTest {

    @Test
    void whenGetNameThenReturnCorrectValue() {
        // given
        IconClass icon = IconClass.RED_CIRCLE;

        // when
        String name = icon.getName();

        // then
        assertEquals("red-circle", name);
    }

    @Test
    void whenEnumValueOfThenReturnCorrectEnum() {
        // when
        IconClass icon = IconClass.valueOf("RED_CIRCLE");

        // then
        assertEquals(IconClass.RED_CIRCLE, icon);
    }

    @Test
    void whenEnumValuesThenContainAllEnums() {
        // when
        IconClass[] values = IconClass.values();

        // then
        assertEquals(1, values.length);
        assertEquals(IconClass.RED_CIRCLE, values[0]);
    }
}