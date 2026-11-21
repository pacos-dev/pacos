package org.pacos.core.component.menu;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.system.proxy.AppProxy;
import org.pacos.core.system.window.OnboardingWindow;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class UserPanelPopoverTest {

    @Test
    void whenCurrentUserIsEligibleThenCreatePopoverWithGettingStartedAction() {
        VaadinMock.mockSystem();

        try (MockedStatic<OnboardingWindow> onboardingWindow = mockStatic(OnboardingWindow.class)) {
            onboardingWindow.when(() -> OnboardingWindow.isEligibleUser(UserSession.getCurrent())).thenReturn(true);

            UserPanelPopover popover = new UserPanelPopover(mock(AppProxy.class), mock(UISystem.class));

            assertNotNull(popover);
            onboardingWindow.verify(() -> OnboardingWindow.isEligibleUser(UserSession.getCurrent()));
        }
    }
}
