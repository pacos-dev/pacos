package org.pacos.core.component.token.service;

import java.time.LocalDate;
import java.util.List;

import org.pacos.base.api.AccessToken;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.token.domain.ApiToken;
import org.pacos.core.component.token.domain.ApiTokenStatus;
import org.pacos.core.component.token.dto.ApiTokenDTO;
import org.pacos.core.component.token.dto.ApiTokenDTOMapper;
import org.pacos.core.component.token.dto.ApiTokenForm;
import org.pacos.core.component.token.repository.ApiTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApiTokenService {

    private final ApiTokenRepository apiTokenRepository;

    public ApiTokenService(ApiTokenRepository apiTokenRepository) {
        this.apiTokenRepository = apiTokenRepository;
    }

    public List<ApiTokenDTO> findAll() {
        return apiTokenRepository.findAllByOrderByNameAsc().stream().map(ApiTokenDTOMapper::map).toList();
    }

    @Transactional("coreTransactionManager")
    public AccessToken createToken(ApiTokenForm tokenForm) {
        if (apiTokenRepository.findById(tokenForm.tokenName()).isPresent()) {
            throw new PacosException("Token with that name already exists");
        }
        String token = TokenGenerator.generateToken();
        ApiToken apiToken = new ApiToken(tokenForm.tokenName(), token, tokenForm.getExpires());
        apiTokenRepository.save(apiToken);
        return new AccessToken(apiToken.getName(), token);
    }

    @Transactional("coreTransactionManager")
    public void revokeToken(String name) {
        apiTokenRepository.updateStatus(name, ApiTokenStatus.REVOKED);
    }

    @Transactional("coreTransactionManager")
    public void removeToken(String name) {
        apiTokenRepository.deleteById(name);
    }

    public boolean isValidToken(String authToken) {
        return apiTokenRepository.isActiveToken(authToken, LocalDate.now(), ApiTokenStatus.ACTIVE);
    }

    @Transactional("coreTransactionManager")
    @Scheduled(cron = "0 1 0 * * *")
    public void invalidateTokens() {
        apiTokenRepository.updateTokenAsExpiredIfExpirationDayReached(LocalDate.now(),
                ApiTokenStatus.ACTIVE,
                ApiTokenStatus.EXPIRED);
    }
}
