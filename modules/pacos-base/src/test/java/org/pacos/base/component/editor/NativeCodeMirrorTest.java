package org.pacos.base.component.editor;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vaadin.addons.variablefield.data.Scope;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class NativeCodeMirrorTest {

    private NativeCodeMirror nativeCodeMirror;
    private Element element;

    @BeforeEach
    void setup() {
        UI mockUi = new UI();
        UI.setCurrent(mockUi);
        nativeCodeMirror = spy(new NativeCodeMirror());
        element = spy(new Element("native-code-editor"));
        doReturn(element).when(nativeCodeMirror).getElement();
    }

    @Test
    void whenInitializeWithScopeThenSetProperty() {
        Scope scope = new Scope("test", 0, 'G', "#4642345");
        nativeCodeMirror = new NativeCodeMirror(List.of(scope));
        //then
        assertEquals("[{\"cssColor\":\"#4642345\",\"id\":0,\"name\":\"test\",\"scopeIcon\":\"G\",\"scopeName\":{\"name\":\"test\"}}]", nativeCodeMirror.getElement().getProperty("scope"));
    }

    @Test
    void whenInitializeWithNullScopeThenPropertyIsNull() {
        nativeCodeMirror = new NativeCodeMirror(null);
        //then
        assertNull(nativeCodeMirror.getElement().getProperty("scope"));
    }


    @Test
    void whenSetLanguageThenLanguageSetOnElement() {
        nativeCodeMirror.setLanguage(ContentMode.JAVA);
        verify(element).executeJs("this.setLanguage($0)", "JAVA");
    }

    @Test
    void whenWithLanguageThenReturnsUpdatedInstance() {
        NativeCodeMirror result = nativeCodeMirror.withLanguage(ContentMode.JAVA);
        verify(element).setProperty("lang", "JAVA");
        assertEquals(nativeCodeMirror, result);
    }

    @Test
    void whenGetValueThenReturnsElementValue() {
        nativeCodeMirror.setValue("sample code");

        assertEquals("sample code", nativeCodeMirror.getValue());
    }

    @Test
    void whenSetValueThenElementPropertyUpdated() {
        nativeCodeMirror.setValue("test code");
        verify(element).setProperty("value", "test code");
    }

    @Test
    void whenWithCompletionThenCompletionSet() {
        CompleteBean completeBean = new CompleteBean("label", "apply", "type", "info");
        nativeCodeMirror.withCompletion(List.of(completeBean));
        verify(element).setPropertyList("completition", List.of(completeBean));
    }

    @Test
    void whenGetRangeSelectionThenListenerTriggeredWithRangeSelect() {
        List<Object> values = new ArrayList<>();
        values.add("selected code");
        values.add(5);
        values.add(10);
        RangeSelectListener listener = Mockito.mock(RangeSelectListener.class);
        PendingJavaScriptResult result = mock(PendingJavaScriptResult.class);

        when(nativeCodeMirror.getElement().executeJs(
                "return this.getValueWithRangeSelection()")).thenReturn(result);
        nativeCodeMirror.getRangeSelection(listener);
        nativeCodeMirror.rangeSelectionCallback(values, listener);

        verify(listener).rangeSelect(new RangeSelect("selected code", 5, 10));
    }
}
