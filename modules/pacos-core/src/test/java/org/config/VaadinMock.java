package org.config;

import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.*;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VaadinMock {


    private VaadinMock() {
    }

    //Hold instance, otherwise garbage collector will remove it from CurrentInstance
    private static VaadinSession vaadinSession;
    //Hold instance, otherwise garbage collector will remove it from CurrentInstance
    private static UserSession userSession;
    //Hold instance, otherwise garbage collector will remove it from CurrentInstance
    private static UI ui;

    public static UISystem mockSystem() {
        return mockSystem(new UserDTO(2, "admin", 0));
    }

    public static UISystem mockSystem(UserDTO userDTO) {
        CurrentInstance.clearAll();

        VaadinMock.ui = Mockito.spy(new UI());
        doNothing().when(VaadinMock.ui).push();
        doReturn(CompletableFuture.completedFuture(null)).when(ui).access(any());
        doReturn(Mockito.mock(Page.class)).when(ui).getPage();
        VaadinMock.userSession = spy(new UserSession(userDTO));
        VaadinMock.vaadinSession = mockVaadinSession();
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(userSession);
        when(vaadinSession.getResourceRegistry()).thenReturn(Mockito.mock(StreamResourceRegistry.class));
        doReturn(userSession).when(vaadinSession).getAttribute(UserSession.class);
        CurrentInstance.set(UserSession.class, userSession);
        CurrentInstance.set(VaadinSession.class, vaadinSession);
        CurrentInstance.set(UI.class, ui);

        ShortcutManager shortcutManager = mock(ShortcutManager.class);
        when(shortcutManager.registerShortcut(any(), any(), any())).thenReturn(mock(ShortcutRegistration.class));

        UISystem uiSystem = spy(new UISystem(mock(DownloadManager.class), mock(VariableManager.class), mock(WindowManager.class),
                mock(ApplicationManager.class), shortcutManager, mock(ClipboardManager.class)));
        userSession.setUiSystem(uiSystem);

        return uiSystem;
    }

    public static void executeAllUiCommands() {
        ArgumentCaptor<Command> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
        verify(UI.getCurrent()).access(commandArgumentCaptor.capture());
        commandArgumentCaptor.getAllValues().forEach(Command::execute);
    }

    public static VaadinSession mockVaadinSession() {
        VaadinSession session = spy(new VaadinSession(Mockito.mock(VaadinService.class)));
        CurrentInstance.set(VaadinSession.class, vaadinSession);
        doNothing().when(session).addRequestHandler(any());
        doNothing().when(session).removeRequestHandler(any());
        doNothing().when(session).lock();
        doNothing().when(session).unlock();
        doNothing().when(session).checkHasLock();
        return session;
    }
}
