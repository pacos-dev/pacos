package org.pacos.config.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacosJdbcServiceTest {

    @Test
    void whenInitDatabaseThenNoException(){
        DataSource dataSource = DataSourceLoader.load();
        //then
        assertDoesNotThrow(()->new PacosJdbcService(dataSource).initDatabase());
    }

    @Test
    void whenDataSourceIsCorruptedThenThrowException(){
        DataSource dataSource = new JDBCDataSource();
        PacosJdbcService pacosJdbcService = new PacosJdbcService(dataSource);
        //then
        assertThrows(PacosJDBCException.class, pacosJdbcService::initDatabase);
    }

    @Test
    void whenTableRemovedThenRecreate(){
        DataSource dataSource = DataSourceLoader.load();
        PacosJdbcService pacosJdbcService = new PacosJdbcService(dataSource);
        pacosJdbcService.initDatabase();
        assertTrue(pacosJdbcService.tableExists("APP_REGISTRY"));
        //when
        execute("DROP TABLE APP_REGISTRY",dataSource);
        //then
        assertFalse(pacosJdbcService.tableExists("APP_REGISTRY"));
        //and then
        pacosJdbcService.initDatabase();
        assertTrue(pacosJdbcService.tableExists("APP_REGISTRY"));
    }

    private static void execute(String query,DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.execute(query);
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }
}