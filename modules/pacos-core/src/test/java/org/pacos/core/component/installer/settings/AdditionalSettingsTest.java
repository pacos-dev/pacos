package org.pacos.core.component.installer.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdditionalSettingsTest {

    @Test
    void whenCreatedWithNoArgsConstructorThenSendingErrorLogAllowedIsTrue() {
        AdditionalSettings additionalSettings = new AdditionalSettings();
        assertTrue(additionalSettings.isSendingErrorLogAllowed());
    }

    @Test
    void whenSetSendingErrorLogAllowedThenFieldIsUpdated() {
        AdditionalSettings additionalSettings = new AdditionalSettings();
        additionalSettings.setSendingErrorLogAllowed(false);
        assertFalse(additionalSettings.isSendingErrorLogAllowed());
    }

    @Test
    void whenSetRegistrationEnabledThenFieldIsUpdated() {
        AdditionalSettings additionalSettings = new AdditionalSettings();
        additionalSettings.setRegistrationEnabled(true);
        assertTrue(additionalSettings.isRegistrationEnabled());
    }

    @Test
    void whenSetGuestAccountEnabledThenFieldIsUpdated() {
        AdditionalSettings additionalSettings = new AdditionalSettings();
        additionalSettings.setGuestAccountEnabled(true);
        assertTrue(additionalSettings.isGuestAccountEnabled());
    }

    @Test
    void whenDefaultConstructorCalledThenFieldsAreFalseExceptSendingErrorLogAllowed() {
        AdditionalSettings additionalSettings = new AdditionalSettings();
        assertFalse(additionalSettings.isRegistrationEnabled());
        assertFalse(additionalSettings.isGuestAccountEnabled());
        assertTrue(additionalSettings.isSendingErrorLogAllowed());
    }
}
