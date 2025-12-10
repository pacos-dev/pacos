package org.pacos.base.component.filepicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.file.FileInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class RootDirsLoaderTest {

    @Test
    void whenFilePathIsNullThenReturnsRootFiles() {
        File mockRoot1 = mock(File.class);
        File mockRoot2 = mock(File.class);
        File[] mockRoots = {mockRoot1, mockRoot2};

        try (MockedStatic<File> fileMock = mockStatic(File.class);
             MockedStatic<FileInfo> fileInfoMock = mockStatic(FileInfo.class)) {
            fileMock.when(File::listRoots).thenReturn(mockRoots);

            when(mockRoot1.exists()).thenReturn(true);
            when(mockRoot2.exists()).thenReturn(true);

            FileInfo mockFileInfo1 = mock(FileInfo.class);
            FileInfo mockFileInfo2 = mock(FileInfo.class);

            fileInfoMock.when(() -> FileInfo.of(mockRoot1)).thenReturn(mockFileInfo1);
            fileInfoMock.when(() -> FileInfo.of(mockRoot2)).thenReturn(mockFileInfo2);

            List<FileInfo> results = RootDirsLoader.getRootDirLocation(null);

            assertEquals(2, results.size());
            assertTrue(results.contains(mockFileInfo1));
            assertTrue(results.contains(mockFileInfo2));
        }
    }

    @Test
    void whenFilePathIsNotNullAndFileExistsThenReturnsFilteredList() {
        File mockFile1 = mock(File.class);
        File mockFile2 = mock(File.class);

        when(mockFile1.exists()).thenReturn(true);
        when(mockFile2.exists()).thenReturn(false);

        FileInfo fileInfo1 = mock(FileInfo.class);
        FileInfo fileInfo2 = mock(FileInfo.class);

        when(fileInfo1.getFile()).thenReturn(mockFile1);
        when(fileInfo2.getFile()).thenReturn(mockFile2);

        List<FileInfo> filePath = List.of(fileInfo1, fileInfo2);

        List<FileInfo> results = RootDirsLoader.getRootDirLocation(filePath);

        assertEquals(1, results.size());
        assertTrue(results.contains(fileInfo1));
        assertFalse(results.contains(fileInfo2));
    }

    @Test
    void whenFilePathIsEmptyThenReturnsEmptyList() {
        List<FileInfo> filePath = new ArrayList<>();

        List<FileInfo> results = RootDirsLoader.getRootDirLocation(filePath);

        assertTrue(results.isEmpty());
    }
}
