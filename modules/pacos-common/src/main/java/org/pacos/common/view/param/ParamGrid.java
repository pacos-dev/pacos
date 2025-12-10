package org.pacos.common.view.param;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import org.pacos.base.component.Theme;
import org.pacos.common.event.ValueChangeListener;

/**
 * @deprecated (since version 1.0.5, it is not clear how this grid exactly works,
 *also param are not unique when added and grid not working correctly)
 */
@Deprecated
public class ParamGrid extends Grid<Param> {

    private final Editor<Param> editor;
    private final transient ParamGridColumnBuilder paramGridColumns;

    private Param last = new Param();

    protected List<Param> items;

    private final List<ValueChangeListener<Param>> listeners = new ArrayList<>();
    public ParamGrid(List<Param> items) {
        super(Param.class, false);
        last.setEnabled(true);
        items.add(last);
        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        addThemeName(Theme.PARAM_GRID.getName());
        this.editor = getEditor();

        this.paramGridColumns = new ParamGridColumnBuilder(this);
        setAllRowsVisible(true);
        setClassNameGenerator(e -> e.equals(last) && editor.getItem() == null ? "last" : null);
        setItems(items);
    }

    public void addValueChangeListener(ValueChangeListener<Param> listener) {
        listeners.add(listener);
    }

    public ParamGrid enableEditor() {
        ParamGridEditor.createEditor(this, paramGridColumns, editor);
        return this;
    }

    public ParamGrid configureParamColumns(String nameHeader, String valueHeader) {
        paramGridColumns.withNameColumn(nameHeader);
        paramGridColumns.withValueColumn(valueHeader);
        paramGridColumns.withButtonColumn();

        removeThemeName(Theme.PARAM_GRID.getName());
        addThemeName(Theme.VALUE_GRID.getName());
        return this;
    }

    public ParamGrid readMode(String nameHeader, String valueHeader) {
        paramGridColumns.withNameColumn(nameHeader);
        paramGridColumns.withValueColumn(valueHeader);

        removeThemeName(Theme.PARAM_GRID.getName());
        addThemeName(Theme.VALUE_GRID.getName());
        return this;
    }

    public void editorCloseListener() {
        if (!last.isEmpty()) {
            last = new Param();
            last.setEnabled(true);
            items.add(last);
        }
        getDataProvider().refreshAll();
        listeners.forEach(e -> e.valueEvent(null));
    }

    public void setItems(List<Param> items) {
        super.setItems(items);
        this.items = items;
        getDataProvider().refreshAll();
    }

    public void removeParam(Param param) {
        items.remove(param);
        getDataProvider().refreshAll();
        listeners.forEach(e -> e.valueEvent(null));
    }

    public boolean isLast(Param param) {
        return last == param;
    }

    public List<Param> getParams() {
        return items.stream().filter(k -> !k.isEmpty()).collect(Collectors.toList());
    }

}
