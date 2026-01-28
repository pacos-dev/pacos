package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.RoleService;
import org.springframework.test.util.ReflectionTestUtils;

import com.vaadin.flow.component.checkbox.Checkbox;

class OnboardingTabLayoutTest {

    private OnboardingTabLayout layout;
    private RoleService roleService;
    private RegistryProxy registryProxy;
    private List<Integer> onboardRoles;

    @BeforeEach
    void setUp() {
        roleService = mock(RoleService.class);
        registryProxy = mock(RegistryProxy.class);
        onboardRoles = new ArrayList<>(List.of(1, 2));

        when(registryProxy.readIntList(RegistryName.ONBOARD_ROLES)).thenReturn(onboardRoles);

        try (var mockedNotification = mockStatic(NotificationUtils.class)) {
            layout = new OnboardingTabLayout(roleService, registryProxy);
        }
    }

    @Test
    void whenCreateCheckBoxBtnCalledThenReturnCheckboxWithCorrectInitialValue() {
        //given
        RoleDTO role = new RoleDTO();
        role.setId(1);

        //when
        Checkbox checkbox = (Checkbox) ReflectionTestUtils.invokeMethod(layout, "createCheckBoxBtn", role);

        //then
        assertNotNull(checkbox);
        assertTrue(checkbox.getValue());
    }

    @Test
    void whenCheckboxValueChangesToTrueThenAddRoleAndSaveRegistry() {
        //given
        RoleDTO role = new RoleDTO();
        role.setId(99);
        role.setLabel("Admin");
        Checkbox checkbox = (Checkbox) ReflectionTestUtils.invokeMethod(layout, "createCheckBoxBtn", role);

        try (var mockedNotification = mockStatic(NotificationUtils.class)) {
            //when
            ReflectionTestUtils.invokeMethod(checkbox, "setModelValue", Boolean.TRUE, true);

            //then
            assertTrue(onboardRoles.contains(99));
            verify(registryProxy).saveList(RegistryName.ONBOARD_ROLES, onboardRoles);
            mockedNotification.verify(() -> NotificationUtils.success(anyString()));
        }
    }

    @Test
    void whenCheckboxValueChangesToFalseThenRemoveRoleAndSaveRegistry() {
        //given
        RoleDTO role = new RoleDTO();
        role.setId(1);
        role.setLabel("User");
        Checkbox checkbox = (Checkbox) ReflectionTestUtils.invokeMethod(layout, "createCheckBoxBtn", role);

        try (var mockedNotification = mockStatic(NotificationUtils.class)) {
            //when
            ReflectionTestUtils.invokeMethod(checkbox, "setModelValue", Boolean.FALSE, true);

            //then
            assertFalse(onboardRoles.contains(1));
            verify(registryProxy).saveList(RegistryName.ONBOARD_ROLES, onboardRoles);
            mockedNotification.verify(() -> NotificationUtils.success(anyString()));
        }
    }

    @Test
    void whenGetSearchIndexCalledThenReturnConcatenatedStrings() {
        //when
        String index = OnboardingTabLayout.getSearchIndex();

        //then
        assertTrue(index.contains("Label"));
        assertTrue(index.contains("Description"));
        assertTrue(index.contains("Roles selected below"));
    }

    @Test
    void whenUpdateRegistryValueCalledThenInvokeProxySave() {
        //when
        ReflectionTestUtils.invokeMethod(layout, "updateRegistryValue");

        //then
        verify(registryProxy).saveList(RegistryName.ONBOARD_ROLES, onboardRoles);
    }
}