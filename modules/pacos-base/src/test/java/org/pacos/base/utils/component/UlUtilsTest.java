package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UlUtilsTest {

    @Test
    void whenOfClassCalledThenListHasClassName() {
        String className = "test-class";
        UlUtils ul = UlUtils.ofClass(className);

        assertEquals(className, ul.getClassName());
    }

    @Test
    void whenWithComponentsCalledThenComponentsAreAdded() {
        Component component1 = new Span();
        Component component2 = new Span();
        UlUtils ul = new UlUtils();
        ul.withComponents(component1, component2);

        assertEquals(2, ul.getChildren().count());
    }

    @Test
    void whenConstructorCalledThenListIsEmptyInitially() {
        UlUtils ul = new UlUtils();

        assertEquals(0, ul.getChildren().count());
    }
}
