package org.pacos.core.component.variable.view.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class UserCollectionValueProviderTest {

    private final UserCollectionValueProvider valueProvider = new UserCollectionValueProvider();

    @Test
    void whenGlobalCollectionThenReturnBoldGlobalLabel() {
        UserVariableCollectionDTO collection = UserVariableCollectionDTO.builder()
                .global(true)
                .name(UserVariableCollectionDTO.GLOBAL_NAME)
                .build();

        Component result = valueProvider.apply(collection);

        assertInstanceOf(Span.class, result);
        assertEquals("Global", result.getElement().getText());
        assertEquals("bold", result.getElement().getStyle().get("font-weight"));
    }

    @Test
    void whenUserCollectionThenReturnCollectionName() {
        UserVariableCollectionDTO collection = UserVariableCollectionDTO.builder()
                .global(false)
                .name("My collection")
                .build();

        Component result = valueProvider.apply(collection);

        assertInstanceOf(Text.class, result);
        assertEquals("My collection", result.getElement().getText());
    }
}
