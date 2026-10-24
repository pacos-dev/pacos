package org.pacos.config.jdbc;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.property.ApplicationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class DataSourceLoaderTest {

    private String originalEnvironment;
    private String originalWorkingDir;
    private String originalDatasourceUrl;

    @BeforeEach
    void useTestEnvironment() {
        originalEnvironment = System.getProperty("pacos.environment");
        originalWorkingDir = System.getProperty("workingDir");
        originalDatasourceUrl = System.getProperty("spring.datasource.url");
        System.setProperty("pacos.environment", "test");
        System.clearProperty("spring.datasource.url");
        ApplicationProperties.reloadProperties();
    }

    @TempDir
    private Path tmpDir;

    @AfterEach
    void restore() {
        restoreSystemProperty("pacos.environment", originalEnvironment);
        restoreSystemProperty("workingDir", originalWorkingDir);
        restoreSystemProperty("spring.datasource.url", originalDatasourceUrl);
        ApplicationProperties.reloadProperties();
    }

    private static void restoreSystemProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
    @Test
    void whenWorkingDirIsUsedInPatchThenUseThisLocation() throws SQLException {
        if(!tmpDir.resolve("test").toFile().mkdirs()){
            fail();
        }
        System.setProperty("workingDir",tmpDir.toString());
        System.setProperty("spring.datasource.url","jdbc:hsqldb:${workingDir}/test/pacos.db;shutdown=true");

        ApplicationProperties.reloadProperties();
        DataSource dataSource = DataSourceLoader.load();
        assertNotNull(dataSource);
        dataSource.getConnection().close();
        File[] files = tmpDir.resolve("test").toFile().listFiles();
        assertNotNull(files);
        assertEquals(2,files.length);
    }

    @Test
    void whenWorkingDirIsNotUsedInPatchThenUseConfiguredValue() {

        System.setProperty("spring.datasource.url","jdbc:hsqldb:mem:db;DB_CLOSE_DELAY=-1");
        System.setProperty("spring.datasource.username","admin");
        System.setProperty("spring.datasource.password","admin");

        DataSource dataSource = DataSourceLoader.load();
        assertNotNull(dataSource);
    }
}