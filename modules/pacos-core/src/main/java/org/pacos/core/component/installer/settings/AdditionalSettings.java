package org.pacos.core.component.installer.settings;

public class AdditionalSettings {

    private boolean registrationEnabled;

    private boolean guestAccountEnabled;

    private boolean sendingErrorLogAllowed;

    private boolean autoUpdateEnabled;

    public AdditionalSettings() {
        this.sendingErrorLogAllowed = true;
        this.autoUpdateEnabled = true;
    }

    public boolean isSendingErrorLogAllowed() {
        return sendingErrorLogAllowed;
    }

    public void setSendingErrorLogAllowed(boolean sendingErrorLogAllowed) {
        this.sendingErrorLogAllowed = sendingErrorLogAllowed;
    }

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public boolean isGuestAccountEnabled() {
        return guestAccountEnabled;
    }

    public void setGuestAccountEnabled(boolean guestAccountEnabled) {
        this.guestAccountEnabled = guestAccountEnabled;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }

    public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
        this.autoUpdateEnabled = autoUpdateEnabled;
    }
}
