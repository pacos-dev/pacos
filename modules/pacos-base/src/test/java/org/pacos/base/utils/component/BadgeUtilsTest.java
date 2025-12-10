package org.pacos.base.utils.component;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BadgeUtilsTest {

    @Test
    void whenCreateThenNoException() {
        Assertions.assertDoesNotThrow(() -> BadgeUtils.create().withText("test"));
    }
}