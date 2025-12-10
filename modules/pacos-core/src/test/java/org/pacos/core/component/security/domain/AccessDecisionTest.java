package org.pacos.core.component.security.domain;

import org.junit.jupiter.api.Test;
import org.pacos.base.session.AccessDecision;

import static org.assertj.core.api.Assertions.assertThat;

class AccessDecisionTest {

    @Test
    void whenEnumValuesThenContainAllowAndDeny() {
        assertThat(AccessDecision.values()).containsExactly(AccessDecision.ALLOW, AccessDecision.DENY);
    }

    @Test
    void whenValueOfAllowThenReturnAllow() {
        assertThat(AccessDecision.valueOf("ALLOW")).isEqualTo(AccessDecision.ALLOW);
    }

    @Test
    void whenValueOfDenyThenReturnDeny() {
        assertThat(AccessDecision.valueOf("DENY")).isEqualTo(AccessDecision.DENY);
    }
}
