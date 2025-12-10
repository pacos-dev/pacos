package org.pacos.config.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.pacos.config.repository.data.AppArtifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleInfoJDBCServiceTest {


    @Test
    void whenTableExistsThenReturnTrue() {
        //given
        ModuleJDBCService jdbcService = new ModuleJDBCService(DataSourceLoader.load());
        //then
        assertTrue(jdbcService.tableExists());
    }


    @Test
    void whenCreateSchemaThenTableFormModuleAreCreated() {
        //given
        ModuleJDBCService jdbcService = new ModuleJDBCService(DataSourceLoader.load());
        //when
        new PacosJdbcService(DataSourceLoader.load()).initDatabase();
        //then
        assertTrue(jdbcService.tableExists());
    }

    @Test
    void whenSaveCurrentVersionOfPacosThenIsStoredInDb() throws SQLException {
        DataSource dataSource = DataSourceLoader.load();
        ModuleJDBCService jdbcService = new ModuleJDBCService(dataSource);
        PacosJdbcService pacosJdbcService = new PacosJdbcService(dataSource);
        pacosJdbcService.initDatabase();
        //when
        jdbcService.saveSystemVersion("1.0.0");
        //then
        try (ResultSet resultSet = executeQuery(jdbcService.getDataSource())) {
            resultSet.next();
            assertEquals("1.0.0", resultSet.getString("VALUE"));
        }
    }

    @Test
    void whenSavePacosConfigurationThenEngineFound() {
        AppArtifact engine = new AppArtifact("org.pacos", "engine", "1.0");
        AppArtifact module = new AppArtifact("org.pacos", "pacos-core", "1.0");
        List<AppArtifact> moduleDTOList = List.of(engine, module);
        ModuleJDBCService jdbcService = new ModuleJDBCService(DataSourceLoader.load());
        //when
        jdbcService.saveModuleConfiguration(moduleDTOList);
        //then
        AppArtifact engineApp = jdbcService.loadEngineModule();
        assertNotNull(engineApp);
        assertEquals(engine.artifactName(), engineApp.artifactName());
    }


    private ResultSet executeQuery(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            return statement.executeQuery("SELECT * FROM APP_REGISTRY WHERE REGISTRY_NAME = 'SYSTEM_VERSION'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}