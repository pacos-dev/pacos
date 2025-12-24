package org.pacos.base.component.filepicker;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.selection.SelectionEvent;
import org.pacos.base.exception.PacosException;
import org.pacos.base.file.FileInfo;
import org.pacos.base.window.DialogJS;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FilePickerDialog extends Dialog {

    private final TreeGrid<FilePickerRow> fileGrid;
    private FilePickerRow selectedItem;

    private final List<FileChangeListener<FileInfo>> valueChangeListeners = new ArrayList<>();

    public FilePickerDialog() {
        this(RootDirsLoader.getRootDirLocation(null), false, null);
    }

    public FilePickerDialog(boolean displayOnlyDir, Set<String> allowedExtension) {
        this(RootDirsLoader.getRootDirLocation(null), displayOnlyDir, allowedExtension);
    }

    /**
     * @param allowedExtension Set of string eg: ".txt",".zip",".log"
     */
    public FilePickerDialog(List<FileInfo> rootDirs, boolean displayOnlyDir, Set<String> allowedExtension) {
        super();

        if (rootDirs == null) {
            throw new PacosException("Root dirs not configured");
        }
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        addThemeName("app-modal");
        addThemeName("app-dialog");
        addClassName("app-window");

        setWidth(450, Unit.PIXELS);
        setHeight(620, Unit.PIXELS);
        setDraggable(true);
        setResizable(true);
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(true);

        getHeader().removeAll();
        Span label = new Span("Choose a file....");
        label.setClassName("no-select");
        label.addClassName("header-label");
        getHeader().add(label);

        this.fileGrid = new TreeGrid<>();
        this.fileGrid.setSizeFull();
        fileGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        fileGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);

        fileGrid.addComponentHierarchyColumn(FilePickerRow::getLabel);
        fileGrid.setDataProvider(new FilePickerDataProvider(rootDirs, displayOnlyDir, allowedExtension));
        fileGrid.addSelectionListener(this::selectionListenerEvent);

        add(fileGrid);
        if (allowedExtension != null && !allowedExtension.isEmpty()) {
            getFooter().add("Extension: " + String.join(", ", allowedExtension));
        }
        for (FileInfo root : rootDirs) {
            fileGrid.expand(new FilePickerRow(root));
        }
        getFooter().add(new Button("Cancel", e -> super.close()));
        getFooter().add(new Button("Ok", e -> okButtonClickEvent()));

    }

    private void okButtonClickEvent() {
        if (selectedItem != null) {
            valueChangeListeners.forEach(listener -> listener.valueEvent(selectedItem.fileInfo()));
        }
        super.close();
    }

    private void selectionListenerEvent(SelectionEvent<Grid<FilePickerRow>, FilePickerRow> e) {
        Optional<FilePickerRow> selectedFileOpt = e.getFirstSelectedItem();
        if (selectedFileOpt.isPresent()) {
            selectedItem = selectedFileOpt.get();
        } else if (selectedItem != null) {
            fileGrid.select(selectedItem);
        }
    }

    public void addValueChangeListener(FileChangeListener<FileInfo> valueChangeListener) {
        this.valueChangeListeners.add(valueChangeListener);
    }

    void setSelectedFile(Path path) {
        if (path != null && path.toFile().exists()) {
            Path cursor = path;
            while (cursor != null) {
                cursor = cursor.getParent();
                if (cursor != null) {
                    FilePickerRow row = new FilePickerRow(FileInfo.of(cursor));
                    fileGrid.expand(row);
                }
            }
            FilePickerRow row = new FilePickerRow(FileInfo.of(path));
            fileGrid.select(row);
        }
    }

    @Override
    public void open() {
        UI.getCurrent().add(this);
        DialogJS.moveToFront(this);
        super.open();
    }

}
