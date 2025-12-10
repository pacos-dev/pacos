package org.pacos.core.config.database;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaVendorAdapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class DatabaseConfigurationTest {

    @Test
    void whenInitializeBeanCoreEntityManagerFactoryThenNoException() {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(mock(Environment.class));
        assertDoesNotThrow(() -> databaseConfiguration.coreEntityManagerFactory(mock(JpaVendorAdapter.class), mock(DataSource.class)));
    }

    @Test
    void whenInitializeBeanCoreJpaVendorAdapterThenNoException() {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(mock(Environment.class));
        assertDoesNotThrow(databaseConfiguration::coreJpaVendorAdapter);
    }


    @Test
    void whenInitializeBeanCoreTransactionManagerThenNoException() {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(mock(Environment.class));
        assertDoesNotThrow(() -> databaseConfiguration.coreTransactionManager(mock(EntityManagerFactory.class)));
    }

    @Test
    void whenInitializeBeanCoreDaraSourceThenNoException() {
        Environment mock = mock(Environment.class);
        doReturn("org.hsqldb.jdbc.JDBCDriver").when(mock).getProperty("spring.datasource.driverClassName");
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(mock);
        assertDoesNotThrow(databaseConfiguration::coreDataSource);
    }

}