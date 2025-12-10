package org.pacos.base.file.archive;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UncompressedNameGeneratorTest {

    @TempDir
    public Path temporaryDir;


    @Test
    void whenGenerateNameForUncompressedThenReturnNameWithoutExtension(){
        Path compressed = temporaryDir.resolve("test.tar.gz");
        //when
        Path name = UncompressedNameGenerator.generateDirectoryName(compressed, temporaryDir, ArchiveType.TAR_GZ);
        assertEquals(temporaryDir.resolve("test"),name);
    }

    @Test
    void whenGenerateNameIsBusyThenReturnNameWithFirstAvailableNumber(){
        temporaryDir.resolve("test").toFile().mkdir();
        Path compressed = temporaryDir.resolve("test.tar.gz");
        //when
        Path name = UncompressedNameGenerator.generateDirectoryName(compressed, temporaryDir, ArchiveType.TAR_GZ);
        assertEquals(temporaryDir.resolve("test_1"),name);
    }

    @Test
    void whenGenerateNameIsBusyThenReturnNameWithNextAvailableNumber(){
        temporaryDir.resolve("test").toFile().mkdir();
        temporaryDir.resolve("test_1").toFile().mkdir();
        temporaryDir.resolve("test_2").toFile().mkdir();
        Path compressed = temporaryDir.resolve("test.tar.gz");
        //when
        Path name = UncompressedNameGenerator.generateDirectoryName(compressed, temporaryDir, ArchiveType.TAR_GZ);
        assertEquals(temporaryDir.resolve("test_3"),name);
    }

    @Test
    void whenGenerateFileNameCalledThenReturnsCorrectPath() {
        Path compressedPath = Path.of("example.zip");
        Path destination = Path.of("/destination");
        ArchiveType type = ArchiveType.ZIP;

        Path result = UncompressedNameGenerator.generateFileName(compressedPath, destination, type);

        assertEquals(destination.resolve("example"), result);
    }
}