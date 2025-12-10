package org.pacos.core.component.token.dto;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.token.domain.ApiToken;
import org.pacos.core.component.token.domain.ApiTokenStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiTokenDTOMapperTest {

    private ApiToken apiToken;

    @BeforeEach
    void setUp() {
        apiToken = mock(ApiToken.class);
    }

    @Test
    void whenMapApiToken_thenFieldsShouldBeMappedCorrectly() {
        // Given
        String name = "testToken";
        String token = "1234567890abcdef";
        LocalDate expiredOn = LocalDate.now().plusDays(1);
        ApiTokenStatus status = ApiTokenStatus.ACTIVE;

        when(apiToken.getName()).thenReturn(name);
        when(apiToken.getToken()).thenReturn(token);
        when(apiToken.getExpiredOn()).thenReturn(expiredOn);
        when(apiToken.getStatus()).thenReturn(status);

        // When
        ApiTokenDTO dto = ApiTokenDTOMapper.map(apiToken);

        // Then
        assertEquals(name, dto.name(), "Name should be mapped correctly.");
        assertEquals(token, dto.token(), "Token should be mapped correctly.");
        assertEquals(expiredOn, dto.expiredOn(), "ExpiredOn should be mapped correctly.");
        assertEquals(status, dto.status(), "Status should be mapped correctly.");
    }
}
