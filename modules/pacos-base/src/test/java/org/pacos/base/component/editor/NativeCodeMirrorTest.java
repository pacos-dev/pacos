package org.pacos.base.component.editor;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;
import elemental.json.Json;
import elemental.json.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vaadin.addons.variablefield.data.Scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertEquals("[{\"name\":\"test\",\"id\":0,\"scopeIcon\":\"G\",\"cssColor\":\"#4642345\",\"scopeName\":{\"name\":\"test\"}}]", nativeCodeMirror.getElement().getProperty("scope"));
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
        JsonArray jsonArray = Json.createArray();
        jsonArray.set(0, Json.create("selected code"));
        jsonArray.set(1, Json.create(5));
        jsonArray.set(2, Json.create(10));

        RangeSelectListener listener = Mockito.mock(RangeSelectListener.class);
        PendingJavaScriptResult result = mock(PendingJavaScriptResult.class);

        when(nativeCodeMirror.getElement().executeJs(
                "return this.getValueWithRangeSelection()")).thenReturn(result);
        nativeCodeMirror.getRangeSelection(listener);
        nativeCodeMirror.rangeSelectionCallback(jsonArray, listener);

        verify(listener).rangeSelect(new RangeSelect("selected code", 5, 10));
    }
}
