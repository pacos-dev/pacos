package org.pacos.core.component.token.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.core.component.token.domain.ApiTokenStatus;
import org.pacos.core.component.token.dto.ApiTokenDTO;
import org.pacos.core.component.token.service.ApiTokenService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiConfigTabLayoutTest {

    @Mock
    private ApiTokenService tokenService;

    @Test
    void whenInitializeTabLayoutThenNoExceptionOnEmptyTokenList() {
        when(tokenService.findAll()).thenReturn(List.of());
        //then
        assertDoesNotThrow(() -> new ApiConfigTabLayout(tokenService));
    }

    @Test
    void whenInitializeTabLayoutThenNoExceptionOnFilledTokenList() {
        when(tokenService.findAll()).thenReturn(
                List.of(
                        new ApiTokenDTO("test", "token", null, true, ApiTokenStatus.ACTIVE),
                        new ApiTokenDTO("test2", "token", LocalDate.now(), false, ApiTokenStatus.REVOKED)
                ));
        //then
        assertDoesNotThrow(() -> new ApiConfigTabLayout(tokenService));
    }

    @Test
    void whenGetSearchIndexThenReturnNotEmptyString(){
        assertNotNull(ApiConfigTabLayout.getSearchIndex());
    }

}