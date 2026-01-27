package org.pacos.base.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.Test;

class ShortUserDTOTest {

    @Test
    void whenCreateShortUserDTOThenReturnConfiguredValues() {
        // given
        ShortUserDTO shortUserDTO = new ShortUserDTO(5, "Test User", List.of());

        // when
        Integer id = shortUserDTO.id();
        String name = shortUserDTO.name();

        // then
        assertEquals(5, id);
        assertEquals("Test User", name);
    }

    @Test
    void whenCheckSerializableThenReturnTrue() {
        // given
        ShortUserDTO shortUserDTO = new ShortUserDTO(5, "Test User", List.of());

        // when
        boolean isSerializable = shortUserDTO instanceof Serializable;

        // then
        assertTrue(isSerializable);
    }
}

