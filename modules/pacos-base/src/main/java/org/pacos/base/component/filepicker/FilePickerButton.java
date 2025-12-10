package org.pacos.base.component.filepicker;

import java.nio.file.Path;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.file.FileInfo;

/**
 * Creates and manage a button action for FilePickerFiled
 */
class FilePickerButton extends Button {

    private final transient FileChangeListener<FileInfo> fileListener;
    private final transient List<FileInfo> startPoint;
    private transient Path selectedFile;
    private FilePickerDialog filePickerDialog;
    private boolean showOnlyDirs;

    FilePickerButton(FileChangeListener<FileInfo> fileListener) {
        this(fileListener, RootDirsLoader.getRootDirLocation(null));
    }

    FilePickerButton(FileChangeListener<FileInfo> fileListener, List<FileInfo> rootDirs) {
        this.fileListener = fileListener;
        this.startPoint = RootDirsLoader.getRootDirLocation(rootDirs);
        setIcon(VaadinIcon.FILE_TREE_SMALL.create());
        addClickListener(e -> openDialog());
        addThemeVariants(ButtonVariant.LUMO_SMALL);
    }

    void openDialog() {
        if (filePickerDialog == null) {
            filePickerDialog = new FilePickerDialog(startPoint, showOnlyDirs,null);
            filePickerDialog.addValueChangeListener(fileListener);
            filePickerDialog.addValueChangeListener(e -> setSelectedFile(e.getFile().toPath()));
            filePickerDialog.setSelectedFile(selectedFile);
        }
        filePickerDialog.open();
    }

    /**
     * Whether file picker should display only directories
     */
    void showOnlyDirs(boolean showOnlyDirs) {
        this.showOnlyDirs = showOnlyDirs;
    }

    void setSelectedFile(Path path) {
        this.selectedFile = path;
    }


    public Path getSelectedFile() {
        return selectedFile;
    }

    public boolean isShowOnlyDirs() {
        return showOnlyDirs;
    }

}
