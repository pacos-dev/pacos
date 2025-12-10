package org.pacos.core.component.token.dto;

import java.time.LocalDate;
import java.util.Objects;

import org.pacos.core.component.token.domain.ApiTokenStatus;

public record ApiTokenDTO(

        String name,
        String token,
        LocalDate expiredOn,
        boolean neverExpired,
        ApiTokenStatus status
) {

    static final String HASH_TOKEN = "*******************";
    public String getExpiredValue() {
        return expiredOn != null ? expiredOn.toString() : "never";
    }

    public String getHashToken() {
        return HASH_TOKEN;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ApiTokenDTO tokenDTO = (ApiTokenDTO) object;
        return Objects.equals(name(), tokenDTO.name());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name());
    }

}
