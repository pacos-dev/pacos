package org.pacos.base.component.filepicker;

import java.io.File;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilePickerFieldTest {

    private FilePickerField filePickerField;
    private Element buttonElement;

    @BeforeEach
    void setUp() {
        buttonElement = mock(Element.class);
        filePickerField = spy(new FilePickerField());
    }

    @Test
    void whenFilePickerFieldInitializedThenHasCorrectPlaceholderAndValueChangeMode() {
        assertEquals("pick a file or enter path", filePickerField.getPlaceholder());
        assertEquals(ValueChangeMode.EAGER, filePickerField.getValueChangeMode());
    }

    @Test
    void whenFocusAndValueEmptyThenFilePickerButtonClicked() {
        doReturn(buttonElement).when(filePickerField).getElement();
        when(filePickerField.getValue()).thenReturn("");

        filePickerField.focus();

        verify(buttonElement, times(1)).executeJs(any(), any());
    }

    @Test
    void whenSetValueWithValidPathThenNoException() {
        String validPath = "valid/path/to/file.txt";

        assertDoesNotThrow(() -> filePickerField.setValue(validPath));
    }

    @Test
    void whenSetValueWithInvalidPathThenNoException() {
        String invalidPath = "\0invalid\0path";

        assertDoesNotThrow(() -> filePickerField.setValue(invalidPath));
    }

    @Test
    void whenSetValueWithNonExistentFileThenDisplaysWarningIcon() {
        String nonExistentFilePath = "non/existent/file.txt";
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(false);

        filePickerField.setValue(nonExistentFilePath);

        assertNotNull(filePickerField.getSuffixComponent());
        assertTrue(filePickerField.getSuffixComponent() instanceof Icon);
        Icon icon = (Icon) filePickerField.getSuffixComponent();
        assertEquals(VaadinIcon.EXCLAMATION_CIRCLE_O.create().getElement().getTag(), icon.getElement().getTag());
    }

    @Test
    void whenSetValueWithNotExistentFileThenSuffixContainsWarningIcon() {
        String existentFilePath = "existent/file.txt";
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(false);

        filePickerField.setValue(existentFilePath);

        assertNotNull(filePickerField.getSuffixComponent());
    }


    @Test
    void whenFileNameIsEmptyThenSuffixComponentIsContContainsWarningIcon() {
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);

        filePickerField.setValue("");

        assertNull(filePickerField.getSuffixComponent());
    }

    @Test
    void whenShowOnlyDirCalledThenFilePickerButtonShowOnlyDirIsCalled() {
        assertDoesNotThrow(() -> filePickerField.showOnlyDir(true));

    }

}