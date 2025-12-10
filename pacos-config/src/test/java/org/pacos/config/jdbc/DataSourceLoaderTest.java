package org.pacos.config.jdbc;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.property.ApplicationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class DataSourceLoaderTest {

    @TempDir
    private Path tmpDir;

    @AfterEach
    void restore(){
        System.setProperty("spring.datasource.url","jdbc:hsqldb:mem:db;DB_CLOSE_DELAY=-1");
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