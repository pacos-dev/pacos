package org.pacos.core.component.token.dto;

import java.time.LocalDate;

public record ApiTokenForm(String tokenName, LocalDate expires, boolean neverExpires) {

    public LocalDate getExpires() {
        return neverExpires ? null : expires;
    }
}
