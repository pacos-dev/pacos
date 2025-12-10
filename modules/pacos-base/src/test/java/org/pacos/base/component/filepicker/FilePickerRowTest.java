package org.pacos.base.component.filepicker;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.file.FileInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilePickerRowTest {

    private FileInfo fileInfo;
    private FilePickerRow filePickerRow;

    @BeforeEach
    void setUp() {
        fileInfo = mock(FileInfo.class);
        filePickerRow = new FilePickerRow(fileInfo);
    }

    @Test
    void whenGetLabelCalledThenReturnsExpectedSpanUtils() {
        File mockFile = mock(File.class);
        when(fileInfo.getFile()).thenReturn(mockFile);
        when(mockFile.isHidden()).thenReturn(true);
        when(fileInfo.getName()).thenReturn("test.txt");

        Span label = filePickerRow.getLabel();

        assertNotNull(label);
        assertFalse(label.getChildren().toList().isEmpty());
    }

    @Test
    void whenGetTypeCalledAndFileIsDirectoryThenReturnsDirectory() {
        File mockFile = mock(File.class);
        when(fileInfo.getFile()).thenReturn(mockFile);
        when(mockFile.isDirectory()).thenReturn(true);

        String type = filePickerRow.getType();

        assertEquals("Directory", type);
    }

    @Test
    void whenGetTypeCalledAndFileHasExtensionThenReturnsExtension() {
        File mockFile = mock(File.class);
        when(fileInfo.getFile()).thenReturn(mockFile);
        when(fileInfo.getName()).thenReturn("example.txt");

        String type = filePickerRow.getType();

        assertEquals("txt", type);
    }

    @Test
    void whenGetTypeCalledAndFileHasNoExtensionThenReturnsEmptyString() {
        File mockFile = mock(File.class);
        when(fileInfo.getFile()).thenReturn(mockFile);
        when(fileInfo.getName()).thenReturn("example");

        String type = filePickerRow.getType();

        assertEquals("", type);
    }

    @Test
    void whenGetNameCalledThenReturnsFileInfoName() {
        when(fileInfo.getName()).thenReturn("example.txt");

        String name = filePickerRow.getName();

        assertEquals("example.txt", name);
    }

    @Test
    void whenGetChildCalledThenReturnsStreamOfFilePickerRows() {
        FileInfo childFileInfo1 = mock(FileInfo.class);
        FileInfo childFileInfo2 = mock(FileInfo.class);
        when(fileInfo.getChild()).thenReturn(List.of(childFileInfo1, childFileInfo2));

        Stream<FilePickerRow> childrenStream = filePickerRow.getChild();

        assertEquals(2, childrenStream.count());
    }

    @Test
    void whenIsDirectoryCalledThenReturnsFileInfoIsDirectory() {
        when(fileInfo.isDirectory()).thenReturn(true);

        boolean isDirectory = filePickerRow.isDirectory();

        assertTrue(isDirectory);
    }
}