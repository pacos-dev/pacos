package org.pacos.base.file;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class AvailableNameGeneratorTest {

    @TempDir
    public Path temporaryDir;

    @Test
    void whenGenerateNameInDirectoryWhereFileNotExistsThenReturnGivenName() {
        //when
        Path name = AvailableNameGenerator.generateName(Path.of("/dir1/dir2/dirName/test.txt"), temporaryDir);
        //then
        assertEquals("test.txt", name.getFileName().toString());
    }

    @Test
    void whenGenerateNameInDirectoryWhereFileExistsThenReturnNewWithIndex() throws IOException {
        if (!temporaryDir.resolve("test.txt").toFile().createNewFile()) {
            fail();
        }
        if (!temporaryDir.resolve("test_1.txt").toFile().createNewFile()) {
            fail();
        }
        //when
        Path name = AvailableNameGenerator.generateName(Path.of("/dir1/dir2/dirName/test.txt"), temporaryDir);
        //then
        assertEquals("test_2.txt", name.getFileName().toString());
    }

    @Test
    void whenFileNameHasDoubleExtensionThenReturnNameWithIndexBeforeFirstIndex() throws IOException {
        if (!temporaryDir.resolve("test.txt.tar.gz").toFile().createNewFile()) {
            fail();
        }
        if (!temporaryDir.resolve("test_1.txt.tar.gz").toFile().createNewFile()) {
            fail();
        }
        //when
        Path name = AvailableNameGenerator.generateName(Path.of("/dir1/dir2/dirName/test.txt.tar.gz"), temporaryDir);
        //then
        assertEquals("test_2.txt.tar.gz", name.getFileName().toString());
    }

    @Test
    void whenFileNameHasNoExtensionThenReturnNameWithoutExtension() throws IOException {
        if (!temporaryDir.resolve("test").toFile().createNewFile()) {
            fail();
        }
        if (!temporaryDir.resolve("test_1").toFile().createNewFile()) {
            fail();
        }
        //when
        Path name = AvailableNameGenerator.generateName(Path.of("/dir1/dir2/dirName/test"), temporaryDir);
        //then
        assertEquals("test_2", name.getFileName().toString());
    }

}