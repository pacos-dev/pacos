package org.pacos.config.property;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Isolated("Shares JVM-wide system properties or other global test resources; keep isolated during parallel runs.")
class WorkingDirTest {

    private Path testWorkingDir;
    private String originalWorkingDir;
    private String originalSystemWorkingDir;
    private String originalOsName;
    private String originalUserHome;

    @BeforeEach
    void setUp() {
        originalWorkingDir = System.getProperty(PropertyName.WORKING_DIR.getPropertyName());
        originalSystemWorkingDir = System.getProperty("workingDir");
        originalOsName = System.getProperty("os.name");
        originalUserHome = System.getProperty("user.home");
        testWorkingDir = Path.of(System.getProperty("java.io.tmpdir"), "test_working_dir");
        System.setProperty("workingDir", testWorkingDir.toString());
        System.setProperty(PropertyName.WORKING_DIR.getPropertyName(), testWorkingDir.toString());
    }

    @AfterEach
    void tearDown() {
        restoreProperty(PropertyName.WORKING_DIR.getPropertyName(), originalWorkingDir);
        restoreProperty("workingDir", originalSystemWorkingDir);
        restoreProperty("os.name", originalOsName);
        restoreProperty("user.home", originalUserHome);
    }

    @Test
    void whenInitializeThenWorkingDirCreated() {
        WorkingDir.initialize();

        assertTrue(Files.isDirectory(testWorkingDir));
    }

    @Test
    void whenInitializeThenLibDirectoryCreated() {
        WorkingDir.initialize();

        assertTrue(Files.isDirectory(testWorkingDir.resolve("lib")));
    }

    @Test
    void whenInitializeOnExistingDirectoryThenNoException() {
        File file = testWorkingDir.toFile();
        if (!file.exists() && !file.mkdirs()) {
            fail("Can't create directory " + file.getAbsolutePath());
        }

        assertDoesNotThrow(WorkingDir::initialize);
    }

    @Test
    void whenGetLibPathThenLibPathReturned() {
        Path libPath = WorkingDir.getLibPath();

        assertEquals(testWorkingDir.resolve("lib"), libPath);
    }

    @Test
    void whenGetModulePathThenReturnPathForValidModuleName() {
        assertEquals(testWorkingDir.resolve("module").resolve("my-module"),
                WorkingDir.getModulePath("my-module"));
        assertEquals(testWorkingDir.resolve("module").resolve("module_2.0"),
                WorkingDir.getModulePath("module_2.0"));
    }

    @Test
    void whenGetModulePathThenRejectPathTraversal() {
        assertThrows(IllegalArgumentException.class, () -> WorkingDir.getModulePath("../outside"));
        assertThrows(IllegalArgumentException.class, () -> WorkingDir.getModulePath("..\\outside"));
    }

    @Test
    void whenGetModulePathThenRejectNullOrEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> WorkingDir.getModulePath(null));
        assertThrows(IllegalArgumentException.class, () -> WorkingDir.getModulePath(""));
    }

    @Test
    void whenWorkingDirIsNotUsedThenReturnDefaultWinLocation() {
        System.clearProperty(PropertyName.WORKING_DIR.getPropertyName());
        System.clearProperty("workingDir");
        System.setProperty("os.name", "win");
        System.setProperty("user.home", "C:\\test");
        WorkingDir.initialize();
        Path path = WorkingDir.load();

        assertEquals(Path.of("C:\\test\\.pacos").toString(), path.toString().replace("/", "\\"));
    }

    @Test
    void whenWorkingDirIsNotUsedThenReturnDefaultOsLocation() {
        System.clearProperty(PropertyName.WORKING_DIR.getPropertyName());
        System.clearProperty("workingDir");
        System.setProperty("os.name", "mac");
        try {
            WorkingDir.initialize();
        } catch (RuntimeException e) {
            // Directory creation may not be available in this environment.
        }
        Path path = WorkingDir.load();

        assertEquals(Path.of("\\opt\\.pacos").toString(), path.toString().replace("/", "\\"));
    }

    @Test
    void whenWorkingDirIsNotUsedThenReturnDefaultLinuxLocation() {
        System.clearProperty(PropertyName.WORKING_DIR.getPropertyName());
        System.clearProperty("workingDir");
        System.setProperty("os.name", "ubuntu");
        try {
            WorkingDir.initialize();
        } catch (RuntimeException e) {
            // Directory creation may not be available in this environment.
        }
        Path path = WorkingDir.load();

        assertEquals(Path.of("\\usr\\local\\.pacos").toString(), path.toString().replace("/", "\\"));
    }

    private static void restoreProperty(String property, String value) {
        if (value == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, value);
        }
    }
}
