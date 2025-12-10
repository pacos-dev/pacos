package org.pacos.core.component.variable.event.user;

import org.junit.jupiter.api.Test;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.view.user.UserVariableForm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FormChangedEventTest {

    @Test
    void whenFireEventThenMarkChangesInTab() {
        //given
        UserVariableForm formMock = mock(UserVariableForm.class);
        TabMonitoring tabMock = mock(TabMonitoring.class);
        when(formMock.getTab()).thenReturn(tabMock);

        //when
        FormChangedEvent.fireEvent(true, formMock);

        //then
        verify(tabMock).markChanges(true);
    }
}
