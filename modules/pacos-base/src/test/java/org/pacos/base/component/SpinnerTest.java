package org.pacos.base.component;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpinnerTest {

    @Test
    void whenCreateThenNoException() {
        Assertions.assertDoesNotThrow(() -> new Spinner().center());
    }
}