package org.pacos.base.file;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReadResourceFileTest {

    static String testFileName = "testResourceFile.txt";


    @Test
    void whenFileExistsThenReadFileReturnsContent() {
        List<String> content = ReadResourceFile.readFile(testFileName);

        assertEquals(3, content.size());
        assertEquals("Line 1", content.get(0));
        assertEquals("Line 2", content.get(1));
        assertEquals("Line 3", content.get(2));
    }

    @Test
    void whenFileDoesNotExistThenReadFileReturnsEmptyList() {
        List<String> content = ReadResourceFile.readFile("nonExistentFile.txt");

        assertTrue(content.isEmpty());
    }

    @Test
    void whenFileCannotBeReadThenReadFileLogsError() {
        List<String> content = ReadResourceFile.readFile("unreadableFile.txt");

        assertTrue(content.isEmpty());
    }
}
