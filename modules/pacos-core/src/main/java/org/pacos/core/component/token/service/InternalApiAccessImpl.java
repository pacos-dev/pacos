package org.pacos.core.component.token.service;

import java.util.Optional;

import org.pacos.base.api.AccessToken;
import org.pacos.base.api.InternalApiAccess;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.token.domain.ApiToken;
import org.pacos.core.component.token.dto.ApiTokenForm;
import org.pacos.core.component.token.repository.ApiTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class InternalApiAccessImpl implements InternalApiAccess {

    private final ApiTokenRepository tokenRepository;
    private final ApiTokenService tokenService;

    public InternalApiAccessImpl(ApiTokenRepository tokenRepository, ApiTokenService tokenService) {
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
    }

    @Override
    public AccessToken receiveToken(String pluginName) {
        if (pluginName == null || pluginName.isEmpty()) {
            throw new PacosException("Plugin name can't be null or empty");
        }
        if (!pluginName.startsWith("plugin_")) {
            throw new PacosException("Plugin name must start with 'plugin_'");
        }
        Optional<ApiToken> optToken = tokenRepository.findById(pluginName);
        return optToken.map(apiToken -> new AccessToken(apiToken.getName(), apiToken.getToken()))
                .orElseGet(() ->
                        tokenService.createToken(new ApiTokenForm(pluginName, null, true)));
    }
}
