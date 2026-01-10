package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.button.Button;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.security.service.UserPermissionService;
import org.pacos.core.component.user.proxy.UserProxyService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserAccountsTabLayoutTest {

    @Mock
    private UserProxyService userProxyService;
    @Mock
    private UserPermissionService userPermissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenInitializeUserAccountsTabLayoutThenCorrect() {
        when(userProxyService.getAllUsers()).thenReturn(List.of(new ShortUserDTO(1, "admin")));
        assertDoesNotThrow(() -> new UserAccountsTabLayout(userProxyService, userPermissionService));
    }

    @Test
    void whenCreatePermissionButtonForAdminThenButtonIsDisabled() {
        Button button = new Button();
        new UserAccountsTabLayout(userProxyService, userPermissionService).createPermissionButton(button,
                new ShortUserDTO(
                        UserDTO.ADMIN_ID, "admin"));
        //then
        assertFalse(button.isEnabled());
    }

    @Test
    void whenCreatePermissionButtonForNonAdminThenButtonIsEnabled() {
        Button button = new Button();
        new UserAccountsTabLayout(userProxyService, userPermissionService).createPermissionButton(button,
                new ShortUserDTO(
                        UserDTO.GUEST_ID, "guest"));
        //then
        assertTrue(button.isEnabled());
    }

    @Test
    void whenModifyUserPermissionThenNoExceptions() {
        VaadinMock.mockSystem();
        ShortUserDTO user = new ShortUserDTO(3, "test");
        when(userProxyService.getAllUsers()).thenReturn(List.of(user));
        //when
        UserAccountsTabLayout tabLayout = new UserAccountsTabLayout(userProxyService, userPermissionService);
        //then
        assertDoesNotThrow(() -> tabLayout.modifyUserPermissionsEvent(user));
    }

    @Test
    void whenGetSearchIndexThenReturnNotEmptyString(){
        assertNotNull(UserAccountsTabLayout.getSearchIndex());
    }
    
}