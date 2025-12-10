package org.pacos.core.component.token.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiTokenStatusTest {

    @ParameterizedTest
    @EnumSource(ApiTokenStatus.class)
    void whenStatusIsActiveThenIsActiveShouldReturnCorrectValue(ApiTokenStatus status) {
        // given

        // when
        boolean result = status.isActive();

        // then
        if (status == ApiTokenStatus.ACTIVE) {
            assertTrue(result);
        } else {
            assertFalse(result);
        }
    }

    @ParameterizedTest
    @EnumSource(ApiTokenStatus.class)
    void whenStatusIsExpiredThenIsExpiredShouldReturnCorrectValue(ApiTokenStatus status) {
        // given

        // when
        boolean result = status.isExpired();

        // then
        if (status == ApiTokenStatus.EXPIRED) {
            assertTrue(result);
        } else {
            assertFalse(result);
        }
    }

    @ParameterizedTest
    @EnumSource(ApiTokenStatus.class)
    void whenStatusIsRevokedThenIsRevokedShouldReturnCorrectValue(ApiTokenStatus status) {
        // given

        // when
        boolean result = status.isRevoked();

        // then
        if (status == ApiTokenStatus.REVOKED) {
            assertTrue(result);
        } else {
            assertFalse(result);
        }
    }
}

