package org.pacos.core.component.security.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppPermissionConfigTest {

    @Test
    void whenConstructAppPermissionConfigThenFieldsAreSet() {
        //given
        Integer id = 1;
        String key = "testKey";
        String label = "testLabel";
        String category = "testCategory";
        String description = "testDescription";
        boolean active = true;

        //when
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(id, key, label, category, description, active);

        //then
        assertEquals(id, appPermissionConfig.getId());
        assertEquals(key, appPermissionConfig.getKey());
        assertEquals(label, appPermissionConfig.getLabel());
        assertEquals(category, appPermissionConfig.getCategory());
        assertEquals(description, appPermissionConfig.getDescription());
        assertTrue(appPermissionConfig.isActive());
    }

    @Test
    void whenGetIdThenReturnId() {
        //given
        Integer id = 1;
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(id, "testKey", "testLabel", "testCategory", "testDescription", true);

        //when
        Integer result = appPermissionConfig.getId();

        //then
        assertEquals(id, result);
    }

    @Test
    void whenGetKeyThenReturnKey() {
        //given
        String key = "testKey";
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(1, key, "testLabel", "testCategory", "testDescription", true);

        //when
        String result = appPermissionConfig.getKey();

        //then
        assertEquals(key, result);
    }

    @Test
    void whenGetLabelThenReturnLabel() {
        //given
        String label = "testLabel";
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(1, "testKey", label, "testCategory", "testDescription", true);

        //when
        String result = appPermissionConfig.getLabel();

        //then
        assertEquals(label, result);
    }

    @Test
    void whenGetCategoryThenReturnCategory() {
        //given
        String category = "testCategory";
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(1, "testKey", "testLabel", category, "testDescription", true);

        //when
        String result = appPermissionConfig.getCategory();

        //then
        assertEquals(category, result);
    }

    @Test
    void whenGetDescriptionThenReturnDescription() {
        //given
        String description = "testDescription";
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(1, "testKey", "testLabel", "testCategory", description, true);

        //when
        String result = appPermissionConfig.getDescription();

        //then
        assertEquals(description, result);
    }

    @Test
    void whenIsActiveThenReturnTrue() {
        //given
        boolean active = true;
        AppPermissionConfig appPermissionConfig = new AppPermissionConfig(1, "testKey", "testLabel", "testCategory", "testDescription", active);

        //when
        boolean result = appPermissionConfig.isActive();

        //then
        assertTrue(result);
    }
}