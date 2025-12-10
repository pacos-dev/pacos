package org.pacos.core.component.token.domain;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiTokenTest {

    @Test
    void whenApiTokenIsCreatedThenNameShouldBeSetCorrectly() {
        // given
        String name = "testToken";
        String token = "abcd1234";
        LocalDate expiredOn = LocalDate.of(2025, 12, 31);

        // when
        ApiToken apiToken = new ApiToken(name, token, expiredOn);

        // then
        assertEquals(name, apiToken.getName());
    }

    @Test
    void whenApiTokenIsCreatedThenStatusShouldBeActiveByDefault() {
        // given
        String name = "testToken";
        String token = "abcd1234";
        LocalDate expiredOn = LocalDate.of(2025, 12, 31);

        // when
        ApiToken apiToken = new ApiToken(name, token, expiredOn);

        // then
        assertEquals(ApiTokenStatus.ACTIVE, apiToken.getStatus());
    }

    @Test
    void whenApiTokenEqualsIsCalledThenTokensWithSameNameAreEqual() {
        // given
        String name = "testToken";
        String token1 = "abcd1234";
        String token2 = "efgh5678";
        LocalDate expiredOn = LocalDate.of(2025, 12, 31);

        // when
        ApiToken apiToken1 = new ApiToken(name, token1, expiredOn);
        ApiToken apiToken2 = new ApiToken(name, token2, expiredOn);

        // then
        assertTrue(apiToken1.equals(apiToken2));
    }

    @Test
    void whenApiTokenEqualsIsCalledThenTokensWithDifferentNamesAreNotEqual() {
        // given
        String name1 = "testToken1";
        String name2 = "testToken2";
        String token = "abcd1234";
        LocalDate expiredOn = LocalDate.of(2025, 12, 31);

        // when
        ApiToken apiToken1 = new ApiToken(name1, token, expiredOn);
        ApiToken apiToken2 = new ApiToken(name2, token, expiredOn);

        // then
        assertFalse(apiToken1.equals(apiToken2));
    }

    @Test
    void whenHashCodeIsCalledThenTokensWithSameNameHaveSameHashCode() {
        // given
        String name = "testToken";
        String token = "abcd1234";
        LocalDate expiredOn = LocalDate.of(2025, 12, 31);

        // when
        ApiToken apiToken1 = new ApiToken(name, token, expiredOn);
        ApiToken apiToken2 = new ApiToken(name, token, expiredOn);

        // then
        assertEquals(apiToken1.hashCode(), apiToken2.hashCode());
    }

    @Test
    void whenHashCodeIsCalledThenTokensWithDifferentNamesHaveDifferentHashCode() {
        // given
        String name1 = "testToken1";
        String name2 = "testToken2";
        String token = "abcd1234";
        LocalDate expiredOn = LocalDate.of(2025, 12, 31);

        // when
        ApiToken apiToken1 = new ApiToken(name1, token, expiredOn);
        ApiToken apiToken2 = new ApiToken(name2, token, expiredOn);

        // then
        assertNotEquals(apiToken1.hashCode(), apiToken2.hashCode());
    }
}
