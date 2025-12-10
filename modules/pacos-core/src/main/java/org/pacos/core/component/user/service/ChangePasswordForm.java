package org.pacos.core.component.user.service;

public class ChangePasswordForm {

    private String currentPassword;
    private String newPassword;
    private String repeatPassword;

    public ChangePasswordForm() {
    }

    public ChangePasswordForm(String currentPassword, String newPassword, String repeatPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.repeatPassword = repeatPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
