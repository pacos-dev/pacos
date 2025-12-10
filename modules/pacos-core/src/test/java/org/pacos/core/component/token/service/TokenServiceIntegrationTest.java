package org.pacos.core.component.token.service;

import java.time.LocalDate;
import java.util.List;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.base.api.AccessToken;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.token.dto.ApiTokenDTO;
import org.pacos.core.component.token.dto.ApiTokenForm;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private ApiTokenService tokenService;

    @Test
    void whenCreateTokenWithTheSameNameThenThrowException() {
        ApiTokenForm revoked = new ApiTokenForm("revoked", LocalDate.now().plusDays(1), true);
        tokenService.createToken(revoked);
        PacosException ex = assertThrows(PacosException.class,
                () -> tokenService.createToken(revoked));
        assertEquals("Token with that name already exists", ex.getMessage());
    }

    @Test
    void whenTokenIsRevokedThenIsNotValid() {
        AccessToken token = tokenService.createToken(
                new ApiTokenForm("revoked", LocalDate.now().plusDays(1), true));
        tokenService.revokeToken("revoked");
        //then
        assertFalse(tokenService.isValidToken(token.token()));
    }

    @Test
    void whenTokenIsActiveThenIsValid() {
        AccessToken token = tokenService.createToken(
                new ApiTokenForm("active", LocalDate.now().plusDays(1), true));
        //then
        assertTrue(tokenService.isValidToken(token.token()));
    }

    @Test
    void whenTokenIsExpiredThenIsNotValid() {
        AccessToken token = tokenService.createToken(
                new ApiTokenForm("revoked", LocalDate.now(), true));
        tokenService.invalidateTokens();
        //then
        assertTrue(tokenService.isValidToken(token.token()));
    }

    @Test
    void whenRemoveTokenThenRemoveFromDB() {
        tokenService.createToken(
                new ApiTokenForm("removed", LocalDate.now().plusDays(1), true));
        //when
        tokenService.removeToken("removed");
        //then
        assertTrue(tokenService.findAll().isEmpty());
    }

    @Test
    void whenRevokeTokenThenChangeStatusToRevoked() {
        tokenService.createToken(
                new ApiTokenForm("revoked", LocalDate.now().plusDays(1), true));
        tokenService.revokeToken("revoked");
        //then
        assertTrue(tokenService.findAll().get(0).status().isRevoked());
    }

    @Test
    void whenMarkTokenAsExpiredThenChangeStatusToExpiredForAllOlderEqualToday() {
        tokenService.createToken(new ApiTokenForm("expired", LocalDate.now().minusDays(1), false));
        tokenService.createToken(new ApiTokenForm("today", LocalDate.now(), false));
        tokenService.createToken(new ApiTokenForm("tomorrow", LocalDate.now().plusDays(1), false));
        tokenService.createToken(new ApiTokenForm("never", null, true));

        tokenService.createToken(new ApiTokenForm("revoked", LocalDate.now().plusDays(1), true));
        tokenService.revokeToken("revoked");
        //when
        tokenService.invalidateTokens();
        //then
        List<ApiTokenDTO> tokens = tokenService.findAll();
        assertEquals(2, tokens.stream().filter(e -> e.status().isExpired()).count());
        assertTrue(tokens.stream().anyMatch(e -> e.status().isExpired() && e.name().equals("expired")));
        assertTrue(tokens.stream().anyMatch(e -> e.status().isExpired() && e.name().equals("today")));

    }
}