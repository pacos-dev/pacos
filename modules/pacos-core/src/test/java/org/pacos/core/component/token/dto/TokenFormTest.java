package org.pacos.core.component.token.dto;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenFormTest {

    @Test
    void whenNeverExpiresIsTrueThenExpiresShouldReturnNull() {
        // Given
        ApiTokenForm tokenForm = new ApiTokenForm("token1", LocalDate.now().plusDays(1), true);

        // When
        LocalDate expires = tokenForm.getExpires();

        // Then
        assertNull(expires, "Expires should be null when neverExpires is true.");
    }

    @Test
    void whenNeverExpiresIsFalseThenExpiresShouldReturnValidDate() {
        // Given
        LocalDate expirationDate = LocalDate.now().plusDays(1);
        ApiTokenForm tokenForm = new ApiTokenForm("token1", expirationDate, false);

        // When
        LocalDate expires = tokenForm.getExpires();

        // Then
        assertEquals(expirationDate, expires, "Expires should return the expiration date when neverExpires is false.");
    }

    @Test
    void whenTokenFormIsCreatedWithConstructorThenFieldsShouldBeSetCorrectly() {
        // Given
        String name = "token1";
        LocalDate expires = LocalDate.now().plusDays(1);
        boolean neverExpires = false;

        // When
        ApiTokenForm tokenForm = new ApiTokenForm(name, expires, neverExpires);

        // Then
        assertEquals(name, tokenForm.tokenName(), "Token name should be set correctly.");
        assertEquals(expires, tokenForm.getExpires(), "Expiration date should be set correctly.");
        assertFalse(tokenForm.neverExpires(), "Never expires flag should be set correctly.");
    }

    @Test
    void whenTokenFormIsCreatedWithoutExpirationDateThenExpiresShouldReturnNullForNeverExpiresTrue() {
        // Given
        ApiTokenForm tokenForm = new ApiTokenForm("token2", null, true);

        // When
        LocalDate expires = tokenForm.getExpires();

        // Then
        assertNull(expires, "Expires should return null when neverExpires is true and expiration date is null.");
    }
}
