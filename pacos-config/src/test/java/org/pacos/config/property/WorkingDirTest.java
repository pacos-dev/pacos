package org.pacos.config.property;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class WorkingDirTest {

    private Path testWorkingDir;

    @BeforeEach
    void setUp() {
        // given
        testWorkingDir = Path.of(System.getProperty("java.io.tmpdir"), "test_working_dir");
        System.setProperty("workingDir", testWorkingDir.toString());
    }

    @Test
    void whenInitializeThenWorkingDirCreated() {
        // when
        WorkingDir.initialize();

        // then
        assertTrue(Files.isDirectory(testWorkingDir));
    }

    @Test
    void whenInitializeThenLibDirectoryCreated() {
        // when
        WorkingDir.initialize();

        // then
        assertTrue(Files.isDirectory(testWorkingDir.resolve("lib")));
    }

    @Test
    void whenInitializeOnExistingFileThenNoException() {
        // given
        File file = new File(testWorkingDir.toString());
        if (!file.exists() && !file.mkdir()) {
            fail("Can't create directory " + file.getAbsolutePath());
        }
        // when, then
        assertDoesNotThrow(WorkingDir::initialize);
    }

    @Test
    void whenGetLibPathThenLibPathReturned() {
        // when
        Path libPath = WorkingDir.getLibPath();

        // then
        assertEquals(testWorkingDir.resolve("lib"), libPath);
    }

    @Test
    void whenWorkingDirIsNotUsedThenReturnDefaultWinLocation() {
        //given
        System.clearProperty("workingDir");
        System.setProperty("os.name", "win");
        System.setProperty("user.home", "C:\\test");
        //when
        WorkingDir.initialize();
        Path path = WorkingDir.load();
        //then
        assertEquals(Path.of("C:\\test\\.pacos").toString(), path.toString().replace("/", "\\"));
    }

    @Test
    void whenWorkingDirIsNotUsedThenReturnDefaultOsLocation() {
        //given
        System.clearProperty("workingDir");
        System.setProperty("os.name", "mac");
        //when
        try {
            WorkingDir.initialize();
        } catch (RuntimeException e) {
            //In case dir can't be created. Not used in this test
        }
        Path path = WorkingDir.load();
        //then
        assertEquals(Path.of("\\opt\\.pacos").toString(), path.toString().replace("/", "\\"));
    }

    @Test
    void whenWorkingDirIsNotUsedThenReturnDefaultLinuxLocation() {
        //given
        System.clearProperty("workingDir");
        System.setProperty("os.name", "ubuntu");
        //when
        try {
            WorkingDir.initialize();
        } catch (RuntimeException e) {
            //In case dir can't be created. Not used in this test
        }
        Path path = WorkingDir.load();
        //then
        assertEquals(Path.of("\\usr\\local\\.pacos").toString(), path.toString().replace("/", "\\"));
    }
}