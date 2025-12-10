package org.pacos.base.component.icon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacosIconTest {

    @Test
    void whenUserSettingIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/user_setting.png", PacosIcon.USER_SETTING.getUrl());
    }

    @Test
    void whenAlertIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/alert.png", PacosIcon.ALERT.getUrl());
    }

    @Test
    void whenVariableIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/variable.png", PacosIcon.VARIABLE.getUrl());
    }

    @Test
    void whenUploadIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/upload.png", PacosIcon.UPLOAD.getUrl());
    }

    @Test
    void whenQuestionIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/question.png", PacosIcon.QUESTION.getUrl());
    }

    @Test
    void whenFileAddIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/file_add.png", PacosIcon.FILE_ADD.getUrl());
    }

    @Test
    void whenWebIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/web.png", PacosIcon.WEB.getUrl());
    }

    @Test
    void whenSettingsIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/settings.png", PacosIcon.SETTINGS.getUrl());
    }

    @Test
    void whenPowerOffIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/power_off.png", PacosIcon.POWER_OFF.getUrl());
    }

    @Test
    void whenLogoutIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/logout.png", PacosIcon.LOGOUT.getUrl());
    }

    @Test
    void whenSystemInfoIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/system_info.png", PacosIcon.SYSTEM_INFO.getUrl());
    }

    @Test
    void whenNoImageIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/noimage.png", PacosIcon.NO_IMAGE.getUrl());
    }

    @Test
    void whenCouplerIconThenGetUrlReturnsCorrectPath() {
        assertEquals("img/icon/pacos.png", PacosIcon.PACOS.getUrl());
    }
}