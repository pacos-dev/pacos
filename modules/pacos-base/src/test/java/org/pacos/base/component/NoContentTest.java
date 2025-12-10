package org.pacos.base.component;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NoContentTest {
    @Test
    void whenCreateThenNoException() {
        assertDoesNotThrow(() -> new NoContent("line1", "line2")
                .width(100)
                .position(10, 20)
                .add(new Span()));
    }

    @Test
    void whenCreateWithDefaultConstructorThenNoException() {
        assertDoesNotThrow(() -> new NoContent());
    }
}