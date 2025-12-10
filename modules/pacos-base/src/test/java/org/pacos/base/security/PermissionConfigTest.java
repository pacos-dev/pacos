package org.pacos.base.security;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.AccessDecision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionConfigTest {

    @Test
    void whenDecisionIsAllowThenReturnAccessDecisionEnum() {
        // given
        PermissionConfig permissionConfig = new PermissionConfig("test-key", "ALLOW");

        // when
        AccessDecision decision = permissionConfig.decision();

        // then
        assertThat(decision).isEqualTo(AccessDecision.ALLOW);
    }

    @Test
    void whenDecisionIsUnknownThenThrowIllegalArgumentException() {
        // given
        PermissionConfig permissionConfig = new PermissionConfig("test-key", "UNKNOWN");

        // when
        ThrowableAssert.ThrowingCallable decisionCall = permissionConfig::decision;

        // then
        assertThatThrownBy(decisionCall)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
    }
}

