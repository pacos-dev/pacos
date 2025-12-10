package org.pacos.base.file.archive;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchiveNameGeneratorTest {

    final String TEST_TAR_GZ = "test.tar.gz";

    @TempDir
    public Path temporaryDir;

    @Test
    void whenGenerateArchiveBaseNameForDirectoryThenReturnOriginalName() {
        //when
        String name = ArchiveNameGenerator.generateArchiveBaseName(Path.of("/dir1/dir2/dirName"));
        //then
        assertEquals("dirName", name);
    }

    @Test
    void whenGenerateArchiveBaseNameForFileWithExtensionThenReturnNameWithoutExtension() {
        //when
        String name = ArchiveNameGenerator.generateArchiveBaseName(Path.of("/dir1/dir2/file.jpg"));
        //then
        assertEquals("file", name);
    }

    @Test
    void whenGenerateArchiveBaseNameForFileWithDoubleExtensionThenReturnNameWithOneExtension() {
        //when
        String name = ArchiveNameGenerator.generateArchiveBaseName(Path.of("/dir1/dir2/file.jpg.gif"));
        //then
        assertEquals("file.jpg", name);
    }

    @Test
    void whenGenerateDestinationPathThenReturnPathWithTarGzExtension() {
        //when
        Path destination =
                ArchiveNameGenerator.generateDestinationLocationPath("test", temporaryDir, ArchiveType.TAR_GZ);
        //then
        assertEquals(TEST_TAR_GZ, destination.getFileName().toString());
    }

    @Test
    void whenGeneratedPathExistsThenReturnNewOneWithAdditionalNumber() throws IOException {
        //given
        temporaryDir.resolve(TEST_TAR_GZ).toFile().createNewFile();
        //when
        Path destination =
                ArchiveNameGenerator.generateDestinationLocationPath("test", temporaryDir, ArchiveType.TAR_GZ);
        //then
        assertEquals("test_1.tar.gz", destination.getFileName().toString());
    }

    @Test
    void whenGeneratedPathExistsThenReturnNewOneWithNextAdditionalNumber() throws IOException {
        //given 
        temporaryDir.resolve(TEST_TAR_GZ).toFile().createNewFile();
        temporaryDir.resolve("test_1.tar.gz").toFile().createNewFile();
        temporaryDir.resolve("test_2.tar.gz").toFile().createNewFile();
        //when
        Path destination =
                ArchiveNameGenerator.generateDestinationLocationPath("test", temporaryDir, ArchiveType.TAR_GZ);
        //then
        assertEquals("test_3.tar.gz", destination.getFileName().toString());
    }

    @Test
    void whenGenerateNameThenReturnCorrectName(){
        //when
        Path destinationPath = ArchiveNameGenerator.generateArchiveName(temporaryDir.resolve("test"),temporaryDir,ArchiveType.ZIP);
        //then
        assertEquals(temporaryDir.resolve("test.zip"),destinationPath);
    }
}