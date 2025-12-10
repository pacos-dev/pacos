package org.pacos.core;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import org.pacos.base.component.editor.NativeCodeMirror;
import org.vaadin.addons.variablefield.field.VariableTextField;
import org.vaadin.addons.variablefield.modal.VariableModal;

//Due to migration to vaadin24 all dynamically used components must be added by annotation @Use to be part of
// the production bundle build
//

@Route(value = "547634678453rfdghtry634564634etgdf")

@Uses(Grid.class)
@Uses(TreeGrid.class)
@Uses(TabSheet.class)
@Uses(SplitLayout.class)
@Uses(MenuBar.class)
@Uses(TextField.class)
@Uses(Button.class)
@Uses(Checkbox.class)
@Uses(TextArea.class)
@Uses(NumberField.class)
@Uses(Select.class)
@Uses(VariableTextField.class)
@Uses(VariableModal.class)
@Uses(HorizontalLayout.class)
@Uses(IntegerField.class)
@Uses(ProgressBar.class)
@Uses(Scroller.class)
@Uses(ComboBox.class)
@Uses(NativeCodeMirror.class)
@Uses(RadioButtonGroup.class)

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

@JsModule("./icons/my-icons-svg.js")
@JsModule("./icons/my-icons-mime.js")
//@JsModule("Frontend/generated/jar-resources/js/codemirror/editor.js")
public class ConfigView extends Span {

    public ConfigView() {
        add(new SplitLayout());
        add(new MenuBar());
        add(new ProgressBar());
        add(new TabSheet());
        add(new TreeGrid<>());
        add(new Grid<>());
        add(new Scroller(new VerticalLayout()));
    }
}
