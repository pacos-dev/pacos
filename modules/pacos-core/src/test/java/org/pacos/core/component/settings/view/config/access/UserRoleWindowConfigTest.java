package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.ShortUserDTO;

class UserRoleWindowConfigTest {

    private UserRoleWindowConfig config;
    private ShortUserDTO shortUserDTO;

    @BeforeEach
    void setUp() {
        shortUserDTO = mock(ShortUserDTO.class);
        when(shortUserDTO.name()).thenReturn("testUser");
        config = new UserRoleWindowConfig(shortUserDTO);
    }

    @Test
    void whenTitleCalledThenReturnTitleWithUserName() {
        //when
        String result = config.title();

        //then
        assertEquals("Roles for user testUser", result);
    }

    @Test
    void whenIconCalledThenReturnUnlockIconUrl() {
        //when
        String result = config.icon();

        //then
        assertEquals(PacosIcon.UNLOCK.getUrl(), result);
    }

    @Test
    void whenActivatorClassCalledThenReturnUserRoleWindowLayout() {
        //when
        Class<?> result = config.activatorClass();

        //then
        assertEquals(UserRoleWindowLayout.class, result);
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        //when
        boolean result = config.isApplication();

        //then
        assertFalse(result);
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnTrue() {
        //when
        boolean result = config.isAllowMultipleInstance();

        //then
        assertTrue(result);
    }

    @Test
    void whenEqualsCalledWithSameShortUserDtoThenReturnTrue() {
        //given
        UserRoleWindowConfig otherConfig = new UserRoleWindowConfig(shortUserDTO);

        //when
        boolean result = config.equals(otherConfig);

        //then
        assertTrue(result);
    }

    @Test
    void whenEqualsCalledWithDifferentShortUserDtoThenReturnFalse() {
        //given
        ShortUserDTO otherUser = mock(ShortUserDTO.class);
        UserRoleWindowConfig otherConfig = new UserRoleWindowConfig(otherUser);

        //when
        boolean result = config.equals(otherConfig);

        //then
        assertFalse(result);
    }

    @Test
    void whenHashCodeCalledThenReturnHashCodeBasedOnShortUserDto() {
        //given
        UserRoleWindowConfig otherConfig = new UserRoleWindowConfig(shortUserDTO);

        //when
        int hashCode1 = config.hashCode();
        int hashCode2 = otherConfig.hashCode();

        //then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void whenGetShortUserDTOCalledThenReturnProvidedDto() {
        //when
        ShortUserDTO result = config.getShortUserDTO();

        //then
        assertEquals(shortUserDTO, result);
    }
}