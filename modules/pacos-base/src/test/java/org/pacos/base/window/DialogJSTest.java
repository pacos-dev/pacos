package org.pacos.base.window;

import java.util.Optional;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.page.Page;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DialogJSTest {

    @Test
    void whenSetPositionThenCallJsImmediately() {
        Dialog dialog = mock(Dialog.class);
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(dialog.getUI()).thenReturn(Optional.of(ui));
        when(ui.getPage()).thenReturn(page);
        //when
        DialogJS.setPositionWithTimeout("0px", "10px", dialog);
        //then
        verify(page).executeJs(any(), eq(dialog), eq("0px"), eq("10px"));
    }
}