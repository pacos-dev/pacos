package org.pacos.core.component.token.view;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.token.domain.ApiTokenStatus;
import org.pacos.core.component.token.dto.ApiTokenDTO;
import org.pacos.core.component.token.service.ApiTokenService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class ApiTokenGridViewTest {

    private final ApiTokenService tokenService = Mockito.mock(ApiTokenService.class);

    @Test
    void whenDeleteBtnClickedThenShowConfirmationWindow() {
        VaadinMock.mockSystem();
        ApiTokenGridView gridView = new ApiTokenGridView(tokenService);
        ButtonUtils deleteBtn = new ButtonUtils("delete");
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);

        gridView.buildDeleteBtn(deleteBtn, dto);
        //when
        deleteBtn.click();
        //then
        verify(UISystem.getCurrent().getWindowManager()).showModalWindow(any(ConfirmationWindowConfig.class));
    }

    @Test
    void whenRevokedBtnClickedThenShowConfirmationWindow() {
        VaadinMock.mockSystem();
        ApiTokenGridView gridView = new ApiTokenGridView(tokenService);
        ButtonUtils deleteBtn = new ButtonUtils("revoke");
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);

        gridView.buildRevokeBtn(deleteBtn, dto);
        //when
        deleteBtn.click();
        //then
        verify(UISystem.getCurrent().getWindowManager()).showModalWindow(any(ConfirmationWindowConfig.class));
    }

    @Test
    void whenDeleteTokenEventThenTriggerService() {
        ApiTokenGridView gridView = new ApiTokenGridView(tokenService);
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);
        //when
        gridView.removeTokenEvent(dto);
        //then
        verify(tokenService).removeToken(dto.name());
    }

    @Test
    void whenRevokeTokenEventThenTriggerService() {
        ApiTokenGridView gridView = new ApiTokenGridView(tokenService);
        ApiTokenDTO dto = new ApiTokenDTO("test", "token", LocalDate.of(2026, 12, 12), false, ApiTokenStatus.ACTIVE);
        //when
        gridView.revokeTokenEvent(dto);
        //then
        verify(tokenService).revokeToken(dto.name());
    }

    @Test
    void whenGetSearchIndexThenReturnNotEmptyString(){
        assertNotNull(ApiTokenGridView.getSearchIndex());
    }
}