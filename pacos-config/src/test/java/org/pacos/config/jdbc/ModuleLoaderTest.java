package org.pacos.config.jdbc;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleLoaderTest {

    @Test
    void whenLoadModulesThenReturnEmptyList(){
        initializeDatabase();

        List<Path> moduleList= ModuleLoader.load();
        //then
        assertEquals(0,moduleList.size());
    }

    @Test
    void whenModuleIsInstalledThenReturnIt(){
        DataSource dataSource = initializeDatabase();
        ModuleJDBCService moduleJDBCService = new ModuleJDBCService(dataSource);
        AppArtifact moduleDTO = new AppArtifact("org.pacos","test","1.0");
        moduleJDBCService.saveModuleConfiguration(List.of(moduleDTO));
        //when
        List<Path> moduleList= ModuleLoader.load();
        //then
        assertEquals(1,moduleList.size());
        assertEquals(WorkingDir.getLibPath().resolve("org/pacos/test/1.0/test-1.0.jar").toString(),moduleList.get(0).toString());
    }

    private static DataSource initializeDatabase() {
        WorkingDir.initialize();
        DataSource dataSource = DataSourceLoader.load();
        new PacosJdbcService(dataSource).initDatabase();
        cleanModuleTable(dataSource);
        return dataSource;
    }

    private static ResultSet cleanModuleTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            return statement.executeQuery("DELETE FROM APP_MODULE");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}