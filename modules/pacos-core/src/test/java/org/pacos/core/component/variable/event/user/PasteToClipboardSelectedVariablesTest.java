package org.pacos.core.component.variable.event.user;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.system.view.PacosJS;

import static org.mockito.Mockito.mockStatic;

class PasteToClipboardSelectedVariablesTest {

    @Test
    void whenVariablesAreEmptyThenDoNotModifyClipboard() {
        try (MockedStatic<PacosJS> mock = mockStatic(PacosJS.class)) {
            //when
            PasteToClipboardSelectedVariables.fireEvent(Set.of());
            //then
            mock.verifyNoInteractions();
        }
    }

    @Test
    void whenVariablesAreSetThenModifyClipboard() {
        UserVariableDTO dto = new UserVariableDTO();
        dto.setName("test");
        dto.setInitialValue("test");


        try (MockedStatic<PacosJS> mock = mockStatic(PacosJS.class);
             MockedStatic<NotificationUtils> not = mockStatic(NotificationUtils.class)) {
            //when
            PasteToClipboardSelectedVariables.fireEvent(Set.of(dto));
            //then
            mock.verify(() -> PacosJS.pasteToClipboard("\"test\":\"test\""));
        }
    }
}