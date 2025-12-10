package org.pacos.base.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreFileFromUploadTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenValidInputStreamAndFileProvidedThenFileIsSavedCorrectly() throws IOException {
        File destinationFile = tempDir.resolve("testFile.txt").toFile();
        byte[] fileContent = "Sample file content".getBytes();
        InputStream fileData = new ByteArrayInputStream(fileContent);

        StoreFileFromUpload.save(destinationFile, fileData);

        assertTrue(destinationFile.exists());
        assertEquals(fileContent.length, destinationFile.length());

        byte[] savedContent = new byte[fileContent.length];
        try (FileInputStream fis = new FileInputStream(destinationFile)) {
            Integer result = fis.read(savedContent);
            assertNotNull(result);
        }
        assertArrayEquals(fileContent, savedContent);
    }

    @Test
    void whenInputStreamIsNullThenThrowsFileSaveException() {
        File destinationFile = tempDir.resolve("testFile.txt").toFile();
        assertThrows(FileUploadException.class, () -> StoreFileFromUpload.save(destinationFile, null));
    }

    @Test
    void whenIOExceptionOccursThenThrowsFileSaveException() {
        File nonExistentDirectoryFile = new File(tempDir.resolve("nonExistentDir").toFile(), "testFile.txt");
        InputStream fileData = new ByteArrayInputStream("Sample content".getBytes());

        assertThrows(FileUploadException.class, () -> StoreFileFromUpload.save(nonExistentDirectoryFile, fileData));
    }
}
