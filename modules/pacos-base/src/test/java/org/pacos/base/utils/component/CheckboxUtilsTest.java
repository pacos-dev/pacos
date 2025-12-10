package org.pacos.base.utils.component;

import com.vaadin.flow.component.HasValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.security.Permission;
import org.pacos.base.session.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckboxUtilsTest {

    private CheckboxUtils checkboxUtils;

    @BeforeEach
    void setUp() {
        checkboxUtils = new CheckboxUtils("Test Checkbox");
    }

    @Test
    void whenWithStyleCalledThenStyleIsApplied() {
        checkboxUtils.withStyle("color", "red");
        assertEquals("red", checkboxUtils.getStyle().get("color"));
    }

    @Test
    void whenWithValueChangeListenerCalledThenListenerIsAdded() {
        var listener = mock(HasValue.ValueChangeListener.class);
        checkboxUtils.withValueChangeListener(listener);
        // Trigger a value change to verify the listener is called
        checkboxUtils.setValue(true);
        verify(listener).valueChanged(any());
    }

    @Test
    void whenWithValueChangeListenerCalledThenListenerIsTriggered() {

        var listener = mock(HasValue.ValueChangeListener.class);
        checkboxUtils.withValueChangeListener(listener);
        checkboxUtils = new CheckboxUtils("Test Checkbox",true,listener);
        // Trigger a value change to verify the listener is called
        checkboxUtils.setValue(false);
        verify(listener).valueChanged(any());
    }

    @Test
    void whenWithWidthCalledThenWidthIsSet() {
        checkboxUtils.withWidth("200px");
        assertEquals("200px", checkboxUtils.getWidth());
    }

    @Test
    void whenWithSelectedCalledThenValueIsTrue() {
        checkboxUtils.withSelected();
        assertTrue(checkboxUtils.getValue());
    }

    @Test
    void whenConstructorCalledWithLabelTextThenLabelIsSet() {
        assertEquals("Test Checkbox", checkboxUtils.getLabel());
    }

    @Test
    void whenWithEnabledForPermissionAllowedThenCheckboxEnabled() {
        // given
        CheckboxUtils checkbox = new CheckboxUtils();
        Permission permission = mock(Permission.class);
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
            when(userSession.hasPermission(permission)).thenReturn(true);

            // when
            checkbox.withEnabledForPermission(permission);

            // then
            assertTrue(checkbox.isEnabled());
            assertNull(checkbox.getTooltip().getText());
        }
    }

    @Test
    void whenWithEnabledForPermissionDeniedThenCheckboxDisabledAndTooltipSet() {
        // given
        CheckboxUtils checkbox = new CheckboxUtils();
        Permission permission = mock(Permission.class);
        when(permission.getKey()).thenReturn("test.permission");
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
            when(userSession.hasPermission(permission)).thenReturn(false);

            // when
            checkbox.withEnabledForPermission(permission);

            // then
            assertFalse(checkbox.isEnabled());
            assertEquals("You do not have permission to access this button", checkbox.getTooltip().getText());
        }
    }
}
