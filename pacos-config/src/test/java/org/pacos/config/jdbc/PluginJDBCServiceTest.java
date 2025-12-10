package org.pacos.config.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.config.repository.data.AppArtifact;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginJDBCServiceTest {


    @BeforeEach
    void init(){
        System.setProperty("spring.datasource.url","jdbc:hsqldb:mem:db;DB_CLOSE_DELAY=-1");
        System.setProperty("spring.datasource.username","admin");
        System.setProperty("spring.datasource.password","admin");
    }
    @Test
    void whenLoadPluginsThenLoadAllDependencies() throws IOException {


        DataSource dataSource = DataSourceLoader.load();
        PluginJDBCService jdbcService = new PluginJDBCService(dataSource);
        new PacosJdbcService(dataSource).initDatabase();

        String testPluginSchema = new String(Objects.requireNonNull(ModuleJDBCService.class.getClassLoader()
                .getResourceAsStream("test_plugin.sql")).readAllBytes());

        String testPluginData = new String(Objects.requireNonNull(ModuleJDBCService.class.getClassLoader()
                .getResourceAsStream("test_plugin_data.sql")).readAllBytes());
        execute(testPluginSchema, jdbcService.getDataSource());
        execute(testPluginData, jdbcService.getDataSource());

        //when
        List<AppArtifact> dependencies = jdbcService.loadPluginsDependencies();
        //then
        assertEquals(2, dependencies.size());
    }

    private void execute(String query, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}