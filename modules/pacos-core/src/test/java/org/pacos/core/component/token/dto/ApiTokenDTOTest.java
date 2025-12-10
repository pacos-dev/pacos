package org.pacos.core.component.token.dto;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.token.domain.ApiTokenStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ApiTokenDTOTest {

    @Test
    void whenGetHashTokenThenReturnStaticValue() {
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", null, true, ApiTokenStatus.ACTIVE);
        //then
        assertEquals("*******************", dto.getHashToken());
    }

    @Test
    void whenNeverExpiredThenReturnNever() {
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", null, true, ApiTokenStatus.ACTIVE);
        //then
        assertEquals("never", dto.getExpiredValue());
    }

    @Test
    void whenNeverExpiredSetToFalseThenReturnDate() {
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);
        //then
        assertEquals("2026-12-12", dto.getExpiredValue());
    }

    @Test
    void whenEqualsWithTheSameNameThenReturnTrue() {
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);
        ApiTokenDTO dto2 = new ApiTokenDTO("test", "token", null, true, ApiTokenStatus.ACTIVE);
        //then
        assertEquals(dto2, dto);
        assertEquals(dto2.hashCode(), dto.hashCode());
    }

    @Test
    void whenEqualsWithDiffClassThenReturnFalse() {
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);
        //then
        assertFalse(dto.equals("ASD"));
    }

    @Test
    void whenEqualsWithDifferentNameThenReturnFalse() {
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);
        ApiTokenDTO dto2 = new ApiTokenDTO("test2", "token", null, true, ApiTokenStatus.ACTIVE);
        //then
        assertNotEquals(dto2, dto);
        assertNotEquals(dto2.hashCode(), dto.hashCode());
    }
}