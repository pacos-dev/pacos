package org.pacos.common.view.param;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.pacos.base.component.Theme;
import org.pacos.common.event.ValueChangeListener;
import org.pacos.common.view.grid.GridMultiSelectExtension;
import org.pacos.common.view.grid.decorator.BlurAndFocusDecorator;
import org.pacos.common.view.param.event.DeleteElementEvent;
import org.pacos.common.view.param.event.PasteFromClipboardEvent;
import org.pacos.common.view.param.event.PasteToClipboardSelectedParam;

/**
 * Grid implementation for param. Enable editing, multiselect, copying and pasting
 */
public class GridParam extends Grid<Param> {

    private final Editor<Param> editor;

    protected Param last;
    protected transient List<Param> items;
    private final List<ValueChangeListener<Param>> listeners = new ArrayList<>();

    public GridParam(List<Param> paramList) {
        this(paramList, false);
    }

    public GridParam(List<Param> paramList, boolean withEnabledColumn) {
        super(Param.class, false);
        setParam(paramList);

        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        if (withEnabledColumn) {
            addThemeName(Theme.PARAM_GRID.getName());
        } else {
            addThemeName(Theme.VALUE_GRID.getName());
        }

        this.editor = getEditor();

        configureColumnsAndEditor(withEnabledColumn);

        setAllRowsVisible(true);
        setClassNameGenerator(e -> e.equals(last) && editor.getItem() == null ? "last" : null);

        GridMultiSelectExtension.extend(this);
        BlurAndFocusDecorator.configureFor(this);
    }

    public void setParam(List<Param> paramList) {
        if (paramList == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = new ArrayList<>(paramList);
        }
        last = new Param();
        last.setEnabled(true);

        items.add(last);
        setItems(items);
    }

    private void configureColumnsAndEditor(boolean withEnabledColumn) {
        if (withEnabledColumn) {
            addColumn(new ComponentRenderer<>(Checkbox::new, this::buildCheckBoxColumn)).setHeader("Enabled");
        }
        Column<Param> nameColumn = addColumn(Param::getName).setResizable(true).setHeader("Name");
        Column<Param> valueColumn = addColumn(Param::getValue).setResizable(true).setHeader("Value");

        addColumn(new ComponentRenderer<>(Button::new, this::buildDeletionButton)).setWidth("20px").setHeader("");
        ParamEditor.createEditor(this, nameColumn, valueColumn, e -> editorCloseListener(e.getItem()));
    }

    private void buildCheckBoxColumn(Checkbox checkBox, Param param) {
        if (isLast(param)) {
            checkBox.setVisible(false);
        }
        checkBox.setValue(param.isEnabled());
        checkBox.addValueChangeListener(e -> changeEnabled(e.getValue(), param));
    }

    private void changeEnabled(boolean value, Param param) {
        param.setEnabled(value);
        listeners.forEach(e -> e.valueEvent(param));
    }

    void buildDeletionButton(Button button, Param param) {
        if (isLast(param)) {
            button.setVisible(false);
        }
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_TERTIARY);
        button.addClassName("grid");
        button.setTooltipText("Remove [del]");
        button.addClickListener(e -> removeParam(param));
        button.setIcon(new Icon(VaadinIcon.TRASH));
    }

    public void editorCloseListener(Param param) {
        if (!last.isEmpty() || !items.contains(last)) {
            last = new Param();
            last.setEnabled(true);
            items.add(last);
            setItems(items);
        }
        getDataProvider().refreshAll();
        listeners.forEach(e -> e.valueEvent(param));
    }

    private void removeParam(Param param) {
        items.remove(param);
        getDataProvider().refreshAll();
        listeners.forEach(e -> e.valueEvent(param));
    }

    public boolean isLast(Param param) {
        return last == param;
    }

    public List<Param> getItems() {
        return items.stream().filter(k -> !k.isEmpty()).collect(Collectors.toList());
    }

    public void addValueChangeListener(ValueChangeListener<Param> listener) {
        this.listeners.add(listener);
    }

    public void modifyItems(List<Param> params) {
        this.items = new ArrayList<>(params);
        editorCloseListener(null);
    }

    /**
     * Copy selected params to user clipBoard. Triggered when grid has a focus.
     *
     * @param doNotCheckFocus if set to true, the focus on grid is not checked
     * @return true when action is taken
     */
    public boolean copySelectedToClipBoard(boolean doNotCheckFocus) {
        boolean focused = isFocused() || doNotCheckFocus;
        if (focused) {
            PasteToClipboardSelectedParam.fireEvent(getSelectedItems());
        }
        return focused;
    }

    /**
     * Paste param from clipBoard. Triggered when grid has a focus.
     *
     * @param doNotCheckFocus if set to true, the focus on grid is not checked
     * @return true when action is taken
     */
    public boolean pasteFromClipBoard(boolean doNotCheckFocus) {
        boolean focused = isFocused() || doNotCheckFocus;
        if (focused) {
            PasteFromClipboardEvent.fireEvent(this);
        }
        return focused;
    }

    /**
     * Delete selected items. Triggered when grid has a focus.
     *
     * @param doNotCheckFocus if set to true, the focus on grid is not checked
     * @return true when action is taken
     */
    public boolean deleteSelectedItems(boolean doNotCheckFocus) {
        boolean focused = isFocused() || doNotCheckFocus;
        if (focused) {
            DeleteElementEvent.fireEvent(this);
        }
        return focused;
    }

    public boolean isFocused() {
        return this.getElement().hasAttribute("focused");
    }
}
