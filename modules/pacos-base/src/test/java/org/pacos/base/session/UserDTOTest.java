package org.pacos.base.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDTOTest {

    @Test
    void whenInitializeUserDTOWithoutIdThenIsGuest(){
        UserDTO userDTO = new UserDTO("User");
        //then
        assertTrue(userDTO.isGuestSession());
    }

    @Test
    void whenInitializeUserDTOWithIdOneThenIsGuest(){
        UserDTO userDTO = new UserDTO(1,"User",0);
        //then
        assertTrue(userDTO.isGuestSession());
    }

    @Test
    void whenInitializeUserDTOWithIdDifferentThanOneThenIsNotGuest(){
        UserDTO userDTO = new UserDTO(2,"User",0);
        //then
        assertFalse(userDTO.isGuestSession());
    }

    @Test
    void whenToStringThenReturnFormattedValue(){
        UserDTO userDTO = new UserDTO(2,"User",0);
        assertEquals("UserDTO={id='2', userName='User'}",userDTO.toString());
    }

    @Test
    void whenSetVariableCollectionThenReturnExpectedValue(){
        UserDTO userDTO = new UserDTO(2,"User",0);
        userDTO.setVariableCollectionId(1);
        //then
        assertEquals(1,userDTO.getVariableCollectionId());
    }

    @Test
    void whenGetVariableCollectionThenReturnConfiguredValue(){
        UserDTO userDTO = new UserDTO(2,"User",0);
        //then
        assertEquals(0,userDTO.getVariableCollectionId());
    }

    @Test
    void whenGetUserNameThenReturnConfiguredValue(){
        UserDTO userDTO = new UserDTO(2,"User",0);
        //then
        assertEquals("User",userDTO.getUserName());
    }

    @Test
    void whenGetIdThenReturnConfiguredValue(){
        UserDTO userDTO = new UserDTO(2,"User",0);
        //then
        assertEquals(2,userDTO.getId());
    }


}