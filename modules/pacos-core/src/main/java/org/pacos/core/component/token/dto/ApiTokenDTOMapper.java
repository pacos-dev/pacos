package org.pacos.core.component.token.dto;

import org.pacos.core.component.token.domain.ApiToken;

public final class ApiTokenDTOMapper {

    private ApiTokenDTOMapper() {
    }

    public static ApiTokenDTO map(ApiToken apiToken) {
        return new ApiTokenDTO(apiToken.getName(),
                apiToken.getToken(),
                apiToken.getExpiredOn(),
                apiToken.getExpiredOn() == null, apiToken.getStatus());
    }

}
