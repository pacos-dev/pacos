package org.pacos.core.component.variable.view.global;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuElementsEnumTest {

    @Test
    void whenGetDisplayNameForAddThenReturnsAddNewVariable() {
        assertEquals("Add new variable", MenuElementsEnum.ADD.getDisplayName());
    }

    @Test
    void whenGetDisplayNameForRemoveThenReturnsRemoveSelectedVariable() {
        assertEquals("Remove selected variable", MenuElementsEnum.REMOVE.getDisplayName());
    }
}
