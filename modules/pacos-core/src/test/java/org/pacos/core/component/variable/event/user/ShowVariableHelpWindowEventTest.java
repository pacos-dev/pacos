package org.pacos.core.component.variable.event.user;

import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.config.VariableHelpConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShowVariableHelpWindowEventTest {

    @Test
    void whenFireEventThenShowVariableHelpWindow() {
        //when
        UserVariableSystem systemMock = mock(UserVariableSystem.class);
        UISystem uiSystemMock = mock(UISystem.class);
        WindowManager windowManagerMock = mock(WindowManager.class);

        when(systemMock.getUiSystem()).thenReturn(uiSystemMock);
        when(uiSystemMock.getWindowManager()).thenReturn(windowManagerMock);

        //when
        ShowVariableHelpWindowEvent.fireEvent(systemMock);

        //then
        verify(windowManagerMock).showWindow(VariableHelpConfig.class);
    }
}
