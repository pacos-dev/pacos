package org.pacos.common.view.param;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParamTest {

    private Param param;

    @BeforeEach
    void setUp() {
        param = new Param("testName", "testValue", true);
    }

    @Test
    void whenConstructParamWithNameAndValueThenValuesAreSet(){
        Param p = new Param("testName", "testValue");
        //then
        assertEquals("testName", p.getName());
        assertEquals("testValue", p.getValue());
    }

    @Test
    void whenConstructParamWithIdNameValueAndEnabledThenValuesAreSet(){
        Param p = new Param(1,"testName", "testValue",true);
        //then
        assertEquals(1, p.getId());
        assertEquals("testName", p.getName());
        assertEquals("testValue", p.getValue());
        assertTrue(p.isEnabled());
    }

    @Test
    void whenGetNameThenReturnCorrectName() {
        assertEquals("testName", param.getName());
    }

    @Test
    void whenSetNameThenNameIsUpdated() {
        param.setName("newName");
        assertEquals("newName", param.getName());
    }

    @Test
    void whenGetValueThenReturnCorrectValue() {
        assertEquals("testValue", param.getValue());
    }

    @Test
    void whenSetValueThenValueIsUpdated() {
        param.setValue("newValue");
        assertEquals("newValue", param.getValue());
    }

    @Test
    void whenSetIdThenIdIsUpdated() {
        param.setId(1);
        assertEquals(1, param.getId());
    }

    @Test
    void whenGetDescriptionThenReturnCorrectDescription() {
        assertNull(param.getDescription());
    }

    @Test
    void whenSetDescriptionThenDescriptionIsUpdated() {
        param.setDescription("newDescription");
        assertEquals("newDescription", param.getDescription());
    }

    @Test
    void whenIsEnabledThenReturnCorrectValue() {
        assertTrue(param.isEnabled());
    }

    @Test
    void whenSetEnabledThenEnabledIsUpdated() {
        param.setEnabled(false);
        assertFalse(param.isEnabled());
    }

    @Test
    void whenIsEmptyForNonEmptyParamThenReturnFalse() {
        assertFalse(param.isEmpty());
    }

    @Test
    void whenIsEmptyForEmptyParamThenReturnTrue() {
        param.setName(null);
        param.setValue(null);
        assertTrue(param.isEmpty());
    }

    @Test
    void whenIsEmptyParamForNonEmptyParamThenReturnFalse() {
        assertFalse(param.isEmptyParam());
    }

    @Test
    void whenIsEmptyParamForEmptyNameAndValueThenReturnTrue() {
        param.setName(null);
        param.setValue(null);
        assertTrue(param.isEmptyParam());
    }

    @Test
    void whenToStringThenReturnExpectedString() {
        String expected = "Param{name='testName', value='testValue', description='null', enabled=true}";
        assertEquals(expected, param.toString());
    }

    @Test
    void whenIsAnyNullForNonNullNameAndValueThenReturnFalse() {
        assertFalse(param.isAnyNull());
    }

    @Test
    void whenIsAnyNullForNullNameThenReturnTrue() {
        param.setName(null);
        assertTrue(param.isAnyNull());
    }

    @Test
    void whenIsAnyNullForNullValueThenReturnTrue() {
        param.setValue(null);
        assertTrue(param.isAnyNull());
    }
}
