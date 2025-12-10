package org.pacos.core.component.token.view;

import java.time.LocalDate;

import com.vaadin.flow.data.binder.BinderValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.exception.PacosException;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.token.dto.ApiTokenForm;
import org.pacos.core.component.token.service.ApiTokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiTokenFormLayoutTest {

    @Mock
    private ApiTokenService tokenService;
    @Mock
    private ApiTokenGridView tokenListView;

    @Test
    void whenNameIsNotGivenThenValidationError() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getNeverExpires().setValue(true);
        //then
        BinderValidationStatus<ApiTokenForm> validate = layout.getFormBinder().validate();
        assertEquals(1, validate.getValidationErrors().size());
        assertEquals("Must have length in range [3-255]", validate.getValidationErrors().get(0).getErrorMessage());
    }

    @Test
    void whenExpirationDateIsSetToPastThenValidationError() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getTokenName().setValue("test");
        layout.getFormBinder().getExpires().setValue(LocalDate.now().minusDays(1));
        //then
        BinderValidationStatus<ApiTokenForm> validate = layout.getFormBinder().validate();
        assertEquals(1, validate.getValidationErrors().size());
        assertEquals("Must be future date", validate.getValidationErrors().get(0).getErrorMessage());
    }

    @Test
    void whenExpirationDateIsSetToTodayThenValidationError() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getTokenName().setValue("test");
        layout.getFormBinder().getExpires().setValue(LocalDate.now());
        //then
        BinderValidationStatus<ApiTokenForm> validate = layout.getFormBinder().validate();
        assertEquals(1, validate.getValidationErrors().size());
        assertEquals("Must be future date", validate.getValidationErrors().get(0).getErrorMessage());
    }

    @Test
    void whenExpirationDateIsSetToTomorrowThenNoValidationError() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getTokenName().setValue("test");
        layout.getFormBinder().getExpires().setValue(LocalDate.now().plusDays(1));
        //then
        assertTrue(layout.getFormBinder().isValid());
    }

    @Test
    void whenNameIsGivenAndNeverExpiredIsCheckedThenNoValidationError() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getNeverExpires().setValue(true);
        layout.getFormBinder().getTokenName().setValue("test");
        //then
        assertTrue(layout.getFormBinder().isValid());
    }

    @Test
    void whenValidationErrorThenDoNotSaveToken() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getTokenName().setValue("test");
        layout.getFormBinder().getExpires().setValue(null);
        //when
        layout.saveTokenEvent();
        //then
        verifyNoInteractions(tokenService);
    }

    @Test
    void whenFormIsValidThenSaveToken() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getTokenName().setValue("test");
        layout.getFormBinder().getExpires().setValue(null);
        layout.getFormBinder().getNeverExpires().setValue(true);
        //when
        layout.saveTokenEvent();
        //then
        verify(tokenService).createToken(new ApiTokenForm("test", null, true));
    }

    @Test
    void whenErrorWhileSaveThenShowNotification() {
        ApiTokenFormLayout layout = new ApiTokenFormLayout(tokenService, tokenListView);
        layout.getFormBinder().getTokenName().setValue("test");
        layout.getFormBinder().getExpires().setValue(null);
        layout.getFormBinder().getNeverExpires().setValue(true);
        PacosException pacosException = new PacosException("Token with that name already exists");
        when(tokenService.createToken(any())).thenThrow(pacosException);

        try (MockedStatic<NotificationUtils> mockedStatic = mockStatic(NotificationUtils.class)) {
            //when
            layout.saveTokenEvent();
            //then
            mockedStatic.verify(() -> NotificationUtils.error("Token with that name already exists"));
        }
    }

}