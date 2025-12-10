package org.pacos.core.component.token.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.api.AccessToken;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.token.domain.ApiToken;
import org.pacos.core.component.token.dto.ApiTokenForm;
import org.pacos.core.component.token.repository.ApiTokenRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalApiAccessImplTest {

    @InjectMocks
    private InternalApiAccessImpl internalApiAccess;
    @Mock
    private ApiTokenService tokenService;
    @Mock
    private ApiTokenRepository apiTokenRepository;

    @Test
    void whenPluginNameIsNullThenThrowException() {
        assertThrows(PacosException.class, () -> internalApiAccess.receiveToken(null));
    }

    @Test
    void whenPluginNameIsEmptyThenThrowException() {
        assertThrows(PacosException.class, () -> internalApiAccess.receiveToken(""));
    }

    @Test
    void whenTokenDoesNotExistThenCreateNewOne() {
        //given
        when(apiTokenRepository.findById("plugin_test")).thenReturn(Optional.empty());
        //when
        internalApiAccess.receiveToken("plugin_test");
        //then
        verify(tokenService).createToken(new ApiTokenForm("plugin_test", null, true));
    }

    @Test
    void whenTokenExistThenReturn() {
        //given
        when(apiTokenRepository.findById("plugin_test")).thenReturn(Optional.of(new ApiToken("plugin_test",
                "test-token",
                null)));
        //when
        AccessToken token = internalApiAccess.receiveToken("plugin_test");
        //then
        assertEquals("plugin_test", token.name());
        assertEquals("test-token", token.token());
        verifyNoInteractions(tokenService);
    }
}