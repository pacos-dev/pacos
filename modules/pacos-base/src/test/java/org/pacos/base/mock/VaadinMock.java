package org.pacos.base.mock;

import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinSession;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.ApplicationManager;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.manager.WindowManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VaadinMock {


    private VaadinMock() {
    }


    //Hold instance, otherwise garbage collector will remove it from CurrentInstance
    private static VaadinSession vaadinSession;
    //Hold instance, otherwise garbage collector will remove it from CurrentInstance
    private static UserSession userSession;
    //Hold instance, otherwise garbage collector will remove it from CurrentInstance
    private static UI ui;

    public static void mockSystem() {
        ui = Mockito.spy(new UI());
        doReturn(Mockito.mock(Page.class)).when(ui).getPage();
        userSession = new UserSession(new UserDTO(1, "admin", 0));
        vaadinSession = Mockito.mock(VaadinSession.class);
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(userSession);
        CurrentInstance.set(UserSession.class, userSession);
        CurrentInstance.set(VaadinSession.class, vaadinSession);
        CurrentInstance.set(UI.class, ui);

        UISystem uiSystem = Mockito.mock(UISystem.class);
        userSession.setUiSystem(uiSystem);
        WindowManager windowManager = Mockito.mock(WindowManager.class);
        when(uiSystem.getWindowManager()).thenReturn(windowManager);

        ApplicationManager manager = Mockito.mock(ApplicationManager.class);
        when(uiSystem.getApplicationManager()).thenReturn(manager);
        ShortcutManager shortcutManager = mock(ShortcutManager.class);
        when(uiSystem.getShortcutManager()).thenReturn(shortcutManager);
        when(shortcutManager.registerShortcut(any(), any(), any())).thenReturn(mock(ShortcutRegistration.class));
    }
}
