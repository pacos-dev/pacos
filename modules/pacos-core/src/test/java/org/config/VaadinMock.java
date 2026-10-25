package org.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.ApplicationManager;
import org.pacos.base.window.manager.ClipboardManager;
import org.pacos.base.window.manager.DownloadManager;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.manager.VariableManager;
import org.pacos.base.window.manager.WindowManager;

import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

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

        UI currentUi = Mockito.spy(new UI());
        VaadinMock.ui = currentUi;
        doNothing().when(currentUi).push();
        doReturn(CompletableFuture.completedFuture(null)).when(currentUi).access(any());
        doReturn(Mockito.mock(Page.class)).when(currentUi).getPage();

        UserSession currentUserSession = spy(new UserSession(userDTO));
        VaadinMock.userSession = currentUserSession;
        VaadinSession currentVaadinSession = mockVaadinSession();
        VaadinMock.vaadinSession = currentVaadinSession;
        when(currentVaadinSession.getAttribute(UserSession.class)).thenReturn(currentUserSession);
        when(currentVaadinSession.getResourceRegistry()).thenReturn(Mockito.mock(StreamResourceRegistry.class));

        CurrentInstance.set(UserSession.class, currentUserSession);
        CurrentInstance.set(VaadinSession.class, currentVaadinSession);
        CurrentInstance.set(UI.class, currentUi);

        ShortcutManager shortcutManager = mock(ShortcutManager.class);
        when(shortcutManager.registerShortcut(any(), any(), any())).thenReturn(mock(ShortcutRegistration.class));
        when(shortcutManager.registerShortcutSequences(any(), any(), any())).thenReturn(mock(ShortcutRegistration.class));
        when(shortcutManager.registerShortcutSequence(any(), any(), any())).thenReturn(mock(Registration.class));

        UISystem uiSystem = spy(new UISystem(mock(DownloadManager.class), mock(VariableManager.class), mock(WindowManager.class),
                mock(ApplicationManager.class), shortcutManager, mock(ClipboardManager.class)));
        currentUserSession.setUiSystem(uiSystem);

        return uiSystem;
    }

    public static void executeAllUiCommands() {
        ArgumentCaptor<Command> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
        verify(UI.getCurrent()).access(commandArgumentCaptor.capture());
        commandArgumentCaptor.getAllValues().forEach(Command::execute);
    }

    public static VaadinSession mockVaadinSession() {
        VaadinSession session = mock(VaadinSession.class);
        VaadinMock.vaadinSession = session;
        CurrentInstance.set(VaadinSession.class, session);
        return session;
    }
}