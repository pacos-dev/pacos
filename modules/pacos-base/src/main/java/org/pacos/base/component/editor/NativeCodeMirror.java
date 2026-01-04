package org.pacos.base.component.editor;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import org.pacos.base.utils.ObjectMapperUtils;
import org.vaadin.addons.variablefield.data.Scope;
import tools.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Objects;

/**
 * Code mirror implementation.
 */
@Tag("native-code-editor")
@NpmPackage(value = "@codemirror/state", version = "^6.0.0")
@NpmPackage(value = "@codemirror/language", version = "^6.0.0")
@NpmPackage(value = "@codemirror/view", version = "^6.0.0")
@NpmPackage(value = "@codemirror/lang-xml", version = "^6.0.0")
@NpmPackage(value = "@codemirror/lang-json", version = "^6.0.0")
@NpmPackage(value = "@codemirror/lang-html", version = "^6.0.0")
@NpmPackage(value = "@codemirror/lang-javascript", version = "^6.0.0")
@NpmPackage(value = "@codemirror/lang-java", version = "^6.0.0")
@NpmPackage(value = "@codemirror/lang-sql", version = "^6.0.0")
@NpmPackage(value = "@codemirror/autocomplete", version = "^6.0.0")
@NpmPackage(value = "codemirror", version = "^6.0.0")
@JsModule("./js/codemirror/editor.js")
public class NativeCodeMirror extends AbstractSinglePropertyField<NativeCodeMirror,
        String> {

    private static final String VALUE_VARIABLE = "value";

    public NativeCodeMirror() {
        super(VALUE_VARIABLE, "", false);
        addAttachListener(event -> this.getUI().ifPresent(
                ui -> ui.getPage().addJavaScript("frontend/js/codemirror/editor.js")));
        //the listener updates current value in the fly
        getElement().addEventListener("value-changed", event -> {
        }).addEventData("event.detail.value");

        setId("native-editor");
        getElement().getStyle().set("width", "100%");
        getElement().getStyle().set("height", "100%");
        getElement().setProperty("lang", ContentMode.JSON.name());
    }

    public NativeCodeMirror(List<Scope> scopes) {
        this();
        if (scopes != null) {
            ArrayNode scopeArray = ObjectMapperUtils.getMapper().valueToTree(scopes);
            getElement().setPropertyJson("scope", scopeArray);
        }
    }

    /**
     * Configure language in which the editor text should be formatted during initialization
     */
    public NativeCodeMirror withLanguage(ContentMode language) {
        getElement().setProperty("lang", language.name());
        return this;
    }

    /**
     * Read current value. The result depends on fetching mode
     */
    @Override
    public String getValue() {
        return Objects.requireNonNullElse(getElement().getProperty(VALUE_VARIABLE), "");
    }

    /**
     * Send value to the editor
     */
    //@Server -> @Client
    @Override
    public void setValue(String value) {
        getElement().setProperty(VALUE_VARIABLE, Objects.requireNonNullElse(value, ""));
    }

    /**
     * Change the language in which the editor text should be formatted
     */
    public void setLanguage(ContentMode language) {
        getElement().executeJs("this.setLanguage($0)", language.name());
    }

    /**
     * Add additional completions to this instance of codeMirror.
     * List will be displayed after insert ctrl+space
     */
    public NativeCodeMirror withCompletion(List<CompleteBean> completion) {
        getElement().setPropertyList("completition", completion);
        return this;
    }

    /**
     * Get selected area of the editor
     */
    public void getRangeSelection(RangeSelectListener listener) {
        getElement().executeJs(
                        "return this.getValueWithRangeSelection()")
                .then(List.class, e -> rangeSelectionCallback(e, listener));
    }

    void rangeSelectionCallback(List<Object> list, RangeSelectListener listener) {
        String contentCode = list.get(0).toString();
        int rangeFrom = (int)list.get(1);
        int rangeTo = (int)list.get(2);
        final RangeSelect value = new RangeSelect(contentCode, rangeFrom, rangeTo);
        listener.rangeSelect(value);
    }

}

