package org.pacos.base.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.exception.PacosException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FileInfoTest {
    private Path tempFile;
    private Path tempFile1;
    private Path tempFile2;
    private Path tempDir;

    @BeforeEach
    void setUp() throws  IOException {
        tempFile = Files.createTempFile("testFile", ".txt");
        tempFile1 = Files.createTempFile("testFile1", ".txt");
        tempFile2 = Files.createTempFile("testFile2", ".txt");
        tempDir = Files.createTempDirectory("testDir");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempFile2);
        Files.deleteIfExists(tempFile2);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void whenInitializeOnNullThenThrowException() {
        File file = null;
        assertThrows(NullPointerException.class, () ->FileInfo.of(file));
    }

    @Test
    void whenInitializeOnNullPathThenThrowException() {
        Path path = null;
        assertThrows(NullPointerException.class, () ->FileInfo.of(path));
    }

    @Test
    void whenFileExistsThenFileInfoCreated() {
        FileInfo fileInfo = FileInfo.of(tempFile.toFile());
        assertEquals(tempFile.toFile(), fileInfo.getFile());
        assertTrue(fileInfo.isFile());
        assertFalse(fileInfo.isDirectory());
    }

    @Test
    void whenFileDoesNotExistThenThrowPacosException() {
        File nonExistentFile = new File("nonExistentFile.txt");
        assertThrows(PacosException.class, () -> FileInfo.of(nonExistentFile));
    }

    @Test
    void whenFileIsDirectoryOrFileThenReturnCorrectType() {
        FileInfo fileInfoFile = FileInfo.of(tempFile.toFile());
        FileInfo fileInfoDir = FileInfo.of(tempDir.toFile());

        assertTrue(fileInfoFile.isFile());
        assertFalse(fileInfoFile.isDirectory());

        assertTrue(fileInfoDir.isDirectory());
        assertFalse(fileInfoDir.isFile());
    }

    @Test
    void whenGetNameCalledThenReturnFileName() {
        FileInfo fileInfo = FileInfo.of(tempFile.toFile());
        assertEquals(tempFile.getFileName().toString(), fileInfo.getName());
    }

    @Test
    void whenNameIsEmptyThenReturnPath() {
        File file = Mockito.mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.length()).thenReturn(0L);
        when(file.getName()).thenReturn("");
        when(file.getPath()).thenReturn("file/path");

        FileInfo fileInfo = FileInfo.of(file);
        assertEquals("file/path", fileInfo.getName());
    }

    @Test
    void whenGetExtensionCalledThenReturnFileExtension() {
        FileInfo fileInfo = FileInfo.of(tempFile.toFile());
        assertEquals("txt", fileInfo.getExtension());

        FileInfo dirInfo = FileInfo.of(tempDir.toFile());
        assertNull(dirInfo.getExtension());
    }

    @Test
    void whenFileChangedThenDetectAndUpdateLength() throws IOException {
        FileInfo fileInfo = FileInfo.of(tempFile.toFile());
        assertFalse(fileInfo.isFileChanged());

        Files.writeString(tempFile, "New content");
        assertTrue(fileInfo.isFileChanged());

        fileInfo.updateFileLength();
        assertFalse(fileInfo.isFileChanged());
    }

    @Test
    void whenDirectoryHasChildrenThenGetChildReturnsList() throws IOException {
        FileInfo dirInfo = FileInfo.of(tempDir.toFile());

        Path childFile = Files.createTempFile(tempDir, "childFile", ".txt");
        assertEquals(1, dirInfo.getChild().size());

        Files.deleteIfExists(childFile);
    }

    @Test
    void whenFileHasNoChildrenThenGetChildReturnsEmptyList() {
        FileInfo fileInfo = FileInfo.of(tempFile.toFile());
        assertTrue(fileInfo.getChild().isEmpty());
    }

    @Test
    void whenToStringCalledThenReturnFormattedString() {
        FileInfo fileInfo = FileInfo.of(tempFile1.toFile());
        String expectedString = "FileInfo={file='" + tempFile1.toFile() + "'}";
        assertEquals(expectedString, fileInfo.toString());
    }

    @Test
    void whenFilesAreEqualThenEqualsReturnsTrue() {
        FileInfo fileInfo1 = FileInfo.of(tempFile1.toFile());
        FileInfo fileInfo2 = FileInfo.of(tempFile1.toFile());
        assertEquals(fileInfo1, fileInfo2);
    }

    @Test
    void whenEqualOnSelfThenTrue() {
        FileInfo fileInfo1 = FileInfo.of(tempFile1.toFile());
        FileInfo fileInfo = fileInfo1;
        assertEquals(fileInfo1, fileInfo);
    }

    @Test
    void whenEqualOnSNullThenFalse() {
        FileInfo fileInfo1 = FileInfo.of(tempFile1.toFile());
        FileInfo fileInfo = null;
        assertNotEquals(fileInfo1, fileInfo);
    }

    @Test
    void whenFilesAreDifferentThenEqualsReturnsFalse() {
        FileInfo fileInfo1 = FileInfo.of(tempFile1.toFile());
        FileInfo fileInfo2 = FileInfo.of(tempFile2.toFile());
        assertNotEquals(fileInfo1, fileInfo2);
    }

    @Test
    void whenFilesAreEqualThenHashCodesAreEqual() {
        FileInfo fileInfo1 = FileInfo.of(tempFile1.toFile());
        FileInfo fileInfo2 = FileInfo.of(tempFile1.toFile());
        assertEquals(fileInfo1.hashCode(), fileInfo2.hashCode());
    }

    @Test
    void whenFilesAreDifferentThenHashCodesAreDifferent() {
        FileInfo fileInfo1 = FileInfo.of(tempFile1.toFile());
        FileInfo fileInfo2 = FileInfo.of(tempFile2.toFile());
        assertNotEquals(fileInfo1.hashCode(), fileInfo2.hashCode());
    }
}