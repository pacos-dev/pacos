package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.springframework.test.util.ReflectionTestUtils;

import com.vaadin.flow.component.button.Button;

class UserAccountsTabLayoutTest {

    private UserAccountsTabLayout layout;
    private UserProxyService userProxyService;

    @BeforeEach
    void setUp() {
        userProxyService = mock(UserProxyService.class);
        layout = new UserAccountsTabLayout(userProxyService);
    }

    @Test
    void whenRefreshGridItemsCalledThenGridItemsAreUpdatedFromService() {
        //given
        ShortUserDTO user = mock(ShortUserDTO.class);
        when(userProxyService.getAllUsers()).thenReturn(List.of(user));

        //when
        ReflectionTestUtils.invokeMethod(layout, "refreshGridItems", userProxyService);

        //then
        verify(userProxyService).getAllUsers();
    }

    @Test
    void whenGetSearchIndexCalledThenReturnConcatenatedHeaders() {
        //when
        String result = UserAccountsTabLayout.getSearchIndex();

        //then
        assertEquals("IDLoginRoles", result);
    }

    @Test
    void whenCreateEditRolesButtonForAdminThenButtonIsDisabled() {
        //given
        Button button = new Button();
        ShortUserDTO adminUser = mock(ShortUserDTO.class);
        when(adminUser.id()).thenReturn(UserDTO.ADMIN_ID);

        //when
        layout.createEditRolesButton(button, adminUser);

        //then
        assertFalse(button.isEnabled());
        assertEquals("Roles", button.getText());
    }

    @Test
    void whenCreateEditRolesButtonForRegularUserThenButtonIsEnabled() {
        //given
        Button button = new Button();
        ShortUserDTO regularUser = mock(ShortUserDTO.class);
        when(regularUser.id()).thenReturn(999);

        //when
        layout.createEditRolesButton(button, regularUser);

        //then
        assertTrue(button.isEnabled());
        assertEquals("Roles", button.getText());
        assertNotNull(button.getIcon());
    }

}