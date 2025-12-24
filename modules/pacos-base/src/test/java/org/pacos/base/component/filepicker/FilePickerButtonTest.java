package org.pacos.base.component.filepicker;

import java.nio.file.Path;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pacos.base.mock.VaadinMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class FilePickerButtonTest {

    @Test
    void whenInitializeObjectThenNoException() {
        assertDoesNotThrow(() -> new FilePickerButton(e -> {
        }));
    }

    @Test
    void whenOpenDialogThenAddWindowToUi() {
        VaadinMock.mockSystem();
        doNothing().when(UI.getCurrent()).add(any(Component.class));
        var captor = ArgumentCaptor.forClass(FilePickerDialog.class);
        //when
        new FilePickerButton(e -> {
        }).openDialog();
        //then
        verify(UI.getCurrent()).add(captor.capture());
    }

    @Test
    void whenOpenDialogTwiceThenAddWindowToUITheSameModal() {
        VaadinMock.mockSystem();
        doNothing().when(UI.getCurrent()).add(any(Component.class));
        var captor = ArgumentCaptor.forClass(FilePickerDialog.class);
        //when
        FilePickerButton filePickerButton = new FilePickerButton(e -> {
        });
        filePickerButton.openDialog();
        filePickerButton.openDialog();
        //then
        verify(UI.getCurrent(), times(2)).add(captor.capture());
        assertEquals(captor.getAllValues().get(0), captor.getAllValues().get(1));
    }

    @Test
    void whenSetOnlyDirThenReturnTrue() {
        FilePickerButton fb = new FilePickerButton(e -> {
        });
        //when
        fb.showOnlyDirs(true);
        //then
        assertTrue(fb.isShowOnlyDirs());
    }

    @Test
    void whenSetSelectedFileThenReturnFile() {
        FilePickerButton fb = new FilePickerButton(e -> {
        });
        Path test = Path.of("test");
        //when
        fb.setSelectedFile(test);
        //then
        assertEquals(test, fb.getSelectedFile());
    }

}