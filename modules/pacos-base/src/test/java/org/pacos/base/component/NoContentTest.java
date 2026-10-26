package org.pacos.base.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.vaadin.flow.component.html.Span;

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
        assertDoesNotThrow((Executable) NoContent::new);
    }
}
