package org.pacos.base.component.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompleteBeanTest {

    @Test
    void whenCompleteBeanCreatedThenFieldsAreSetCorrectly() {
        CompleteBean bean = new CompleteBean("Label", "Apply", "Type", "Info");

        assertEquals("Label", bean.label());
        assertEquals("Apply", bean.apply());
        assertEquals("Type", bean.type());
        assertEquals("Info", bean.info());
    }

    @Test
    void whenCompleteBeanEqualityThenEqualObjects() {
        CompleteBean bean1 = new CompleteBean("Label", "Apply", "Type", "Info");
        CompleteBean bean2 = new CompleteBean("Label", "Apply", "Type", "Info");

        assertEquals(bean1, bean2);
    }

    @Test
    void whenCompleteBeanToStringThenCorrectFormat() {
        CompleteBean bean = new CompleteBean("Label", "Apply", "Type", "Info");

        assertTrue(bean.toString().contains("Label"));
        assertTrue(bean.toString().contains("Apply"));
        assertTrue(bean.toString().contains("Type"));
        assertTrue(bean.toString().contains("Info"));
    }
}
