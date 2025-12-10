package org.pacos.base.component.filepicker;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.pacos.base.component.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component creates a text field with a button. After button click, modal window with explorer is displayed,
 * and user is able to choose a file. After confirmation, file name is displayed in text field.
 */
public class FilePickerField extends TextField {

    private final FilePickerButton filePicker;
    private static final Logger LOG = LoggerFactory.getLogger(FilePickerField.class);

    public FilePickerField() {
        this.filePicker = new FilePickerButton(e -> setValue(e.getFile().getAbsolutePath()));
        addToPrefix(filePicker);
        setPlaceholder("pick a file or enter path");
        setValueChangeMode(ValueChangeMode.EAGER);
        addValueChangeListener(e -> setValue(e.getValue()));
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (!value.isEmpty()) {
            try {
                filePicker.setSelectedFile(Path.of(value));
            } catch (InvalidPathException e) {
                LOG.error("Invalid path: {}", value);
            }
        }
        if (!value.isEmpty() && !new File(value).exists()) {
            Icon icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
            icon.setColor(Color.RED.getColor());
            icon.getTooltip().setText("This file does not exists");
            setSuffixComponent(icon);
        } else {
            setSuffixComponent(null);
        }
    }

    public void showOnlyDir(boolean showDir) {
        filePicker.showOnlyDirs(showDir);
    }

}
