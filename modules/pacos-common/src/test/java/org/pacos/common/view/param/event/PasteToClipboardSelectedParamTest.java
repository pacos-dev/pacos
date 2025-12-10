package org.pacos.common.view.param.event;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import org.junit.jupiter.api.Test;
import org.pacos.common.view.param.Param;
import org.pacos.common.view.param.ParamParser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PasteToClipboardSelectedParamTest {

    @Test
    void whenSelectedItemsEmptyThenNoReaction() {
        UI mockUi = mock(UI.class);
        UI.setCurrent(mockUi);
        //when
        PasteToClipboardSelectedParam.fireEvent(new HashSet<>());
        //then
        verifyNoInteractions(mockUi);
    }

    @Test
    void whenSelectedItemsThenPasteToClipboard() {
        UI mockUi = mock(UI.class);
        Page pageMock = mock(Page.class);
        when(mockUi.getPage()).thenReturn(pageMock);
        UI.setCurrent(mockUi);
        //when
        Set<Param> selected = Set.of(new Param("test", "test", true));
        PasteToClipboardSelectedParam.fireEvent(selected);
        //then
        verify(pageMock).executeJs("""
                        navigator.clipboard.writeText($0)
                        .catch(err => console.error('Error writing text to clipboard', err));
                        """
                , ParamParser.mapToString(selected.stream().toList()));
    }
}