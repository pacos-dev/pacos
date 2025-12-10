package org.pacos.base.utils.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.component.Theme;
import org.pacos.base.security.Permission;
import org.pacos.base.session.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ButtonUtilsTest {

    private ButtonUtils buttonUtils;

    @BeforeEach
    void setUp() {
        buttonUtils = new ButtonUtils("Test Button");
    }

    @Test
    void whenWithVariantsCalledThenVariantsAreAdded() {
        ButtonVariant[] variants = {ButtonVariant.LUMO_PRIMARY};
        buttonUtils.withVariants(variants);
        assertTrue(buttonUtils.getThemeNames().contains("primary"));
    }

    @Test
    void whenWithThemesCalledThenThemesAreAdded() {
        buttonUtils.withThemes("theme1", "theme2");
        assertTrue(buttonUtils.getThemeNames().contains("theme1"));
        assertTrue(buttonUtils.getThemeNames().contains("theme2"));
    }

    @Test
    void whenWithClickListenerCalledThenListenerIsAdded() {
        var listener = mock(ComponentEventListener.class);
        buttonUtils.withClickListener(listener);
        buttonUtils.click(); // Simulate a click event
        verify(listener).onComponentEvent(any());
    }

    @Test
    void whenWithClassNameCalledThenClassNameIsAdded() {
        buttonUtils.withClassName("test-class");
        assertTrue(buttonUtils.getClassNames().contains("test-class"));
    }

    @Test
    void whenWithEnabledCalledThenButtonEnabledStatusIsUpdated() {
        buttonUtils.withEnabled(false);
        assertFalse(buttonUtils.isEnabled());
    }

    @Test
    void whenWithStyleCalledThenStyleIsApplied() {
        buttonUtils.withStyle("background-color", "red");
        assertEquals("red", buttonUtils.getStyle().get("background-color"));
    }

    @Test
    void whenFloatRightCalledThenButtonIsFloatedRight() {
        buttonUtils.floatRight();
        assertEquals("right", buttonUtils.getStyle().get("float"));
    }

    @Test
    void whenHiddenCalledThenButtonIsNotVisible() {
        buttonUtils.hidden();
        assertFalse(buttonUtils.isVisible());
    }

    @Test
    void whenWithWidthCalledThenWidthIsSet() {
        buttonUtils.withWidth("100px");
        assertEquals("100px", buttonUtils.getWidth());
    }

    @Test
    void whenWithTooltipCalledThenTooltipTextIsSet() {
        buttonUtils.withTooltip("Test Tooltip");
        assertEquals("Test Tooltip", buttonUtils.getTooltip().getText());
    }

    @Test
    void whenInfoLayoutCalledThenInfoLayoutThemeIsAdded() {
        buttonUtils.infoLayout();
        assertTrue(buttonUtils.getThemeNames().contains("info_l"));
    }

    @Test
    void whenTabsheetBtnCalledThenTabsheetBtnThemeAndVariantAreAdded() {
        buttonUtils.tabsheetBtn();
        assertTrue(buttonUtils.getThemeNames().contains("tab"));
        assertTrue(buttonUtils.getThemeNames().contains(ButtonVariant.LUMO_ICON.getVariantName()));
    }

    @Test
    void whenErrorLayoutCalledThenErrorLayoutThemeIsAdded() {
        buttonUtils.errorLayout();
        assertTrue(buttonUtils.getThemeNames().contains("error_l"));
    }

    @Test
    void whenSuccessLayoutCalledThenSuccessLayoutThemeIsAdded() {
        buttonUtils.successLayout();
        assertTrue(buttonUtils.getThemeNames().contains("success_l"));
    }

    @Test
    void whenPrimaryLayoutCalledThenPrimaryLayoutThemeIsAdded() {
        buttonUtils.primaryLayout();
        assertTrue(buttonUtils.getThemeNames().contains("primary_l"));
    }

    @Test
    void whenTabCloseBtnCalledThenTabCloseBtnThemeAndVariantsAreAdded() {
        buttonUtils.tabCloseBtn();
        assertTrue(buttonUtils.getThemeNames().contains(Theme.TAB_CLOSE.getName()));
        assertTrue(buttonUtils.getThemeNames().contains(ButtonVariant.LUMO_SMALL.getVariantName()));
        assertTrue(buttonUtils.getThemeNames().contains(ButtonVariant.LUMO_TERTIARY.getVariantName()));
    }

    @Test
    void whenButtonWithIconAndListenerCreatedThenIconAndListenerAreSet() {
        ComponentEventListener<ClickEvent<Button>> listener = mock(ComponentEventListener.class);
        ButtonUtils buttonWithListener = new ButtonUtils(VaadinIcon.EDIT).withClickListener(listener);
        buttonWithListener.click();
        verify(listener).onComponentEvent(any());
    }

    @Test
    void whenButtonWithTextAndIconCreatedThenTextAndIconAreSet() {
        Icon icon = VaadinIcon.EDIT.create();
        ButtonUtils buttonWithIcon = new ButtonUtils("Test Button", icon);
        assertEquals("Test Button", buttonWithIcon.getText());
        assertTrue(buttonWithIcon.getChildren().anyMatch(child -> child.equals(icon)));
    }

    @Test
    void whenButtonWithTextAndClickListenerCreatedThenClickListenerIsSet() {
        ComponentEventListener<ClickEvent<Button>> listener = mock(ComponentEventListener.class);
        ButtonUtils buttonWithListener = new ButtonUtils("Test Button", listener);
        buttonWithListener.click();
        verify(listener).onComponentEvent(any());
    }

    @Test
    void whenButtonWithIconAndClickListenerCreatedThenIconAndClickListenerAreSet() {
        Icon icon = VaadinIcon.EDIT.create();
        ComponentEventListener<ClickEvent<Button>> listener = mock(ComponentEventListener.class);
        ButtonUtils buttonWithIconListener = new ButtonUtils(icon, listener);
        buttonWithIconListener.click();
        verify(listener).onComponentEvent(any());
    }

    @Test
    void whenButtonWithTextIconAndClickListenerCreatedThenAllAreSet() {
        Icon icon = VaadinIcon.EDIT.create();
        ComponentEventListener<ClickEvent<Button>> listener = mock(ComponentEventListener.class);
        ButtonUtils buttonWithAll = new ButtonUtils("Test Button", icon, listener);
        buttonWithAll.click();
        assertEquals("Test Button", buttonWithAll.getText());
        verify(listener).onComponentEvent(any());
    }

    @Test
    void whenButtonWithIconThenIconIsSet(){
        Icon icon = VaadinIcon.EDIT.create();
        ButtonUtils buttonUtils = new ButtonUtils(icon);

        assertEquals(buttonUtils.getIcon(),icon);
    }

    @Test
    void whenButtonThenIconAndTestIsNotSet(){
        ButtonUtils buttonUtils = new ButtonUtils();

        assertNull(buttonUtils.getIcon());
        assertEquals("",buttonUtils.getText());
    }

    @Test
    void whenWithVisibleForPermissionAllowedThenButtonVisible() {
        // given
        ButtonUtils buttonUtils = new ButtonUtils();
        Permission permission = mock(Permission.class);
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
            when(userSession.hasPermission(permission)).thenReturn(true);

            // when
            buttonUtils.withVisibleForPermission(permission);

            // then
            assertTrue(buttonUtils.isVisible());
        }
    }

    @Test
    void whenWithVisibleForPermissionDeniedThenButtonNotVisible() {
        // given
        ButtonUtils buttonUtils = new ButtonUtils();
        Permission permission = mock(Permission.class);
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
            when(userSession.hasPermission(permission)).thenReturn(false);

            // when
            buttonUtils.withVisibleForPermission(permission);

            // then
            assertFalse(buttonUtils.isVisible());
        }
    }

    @Test
    void whenWithEnabledForPermissionAllowedThenButtonEnabled() {
        // given
        ButtonUtils buttonUtils = new ButtonUtils();
        Permission permission = mock(Permission.class);
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
            when(userSession.hasPermission(permission)).thenReturn(true);

            // when
            buttonUtils.withEnabledForPermission(permission);

            // then
            assertTrue(buttonUtils.isEnabled());
            assertNull(buttonUtils.getTooltip().getText());
        }
    }

    @Test
    void whenWithEnabledForPermissionDeniedThenButtonDisabledAndTooltipSet() {
        // given
        ButtonUtils buttonUtils = new ButtonUtils();
        Permission permission = mock(Permission.class);
        when(permission.getKey()).thenReturn("test.permission");
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
            when(userSession.hasPermission(permission)).thenReturn(false);

            // when
            buttonUtils.withEnabledForPermission(permission);

            // then
            assertFalse(buttonUtils.isEnabled());
            assertEquals("You do not have permission to access this button", buttonUtils.getTooltip().getText());
        }
    }
}
