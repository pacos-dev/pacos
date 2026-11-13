package org.pacos.core.component.registry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryValueMigrationTest {

    private static final String MIGRATION =
            "db/migration/core/V1_2026_11_13_19_40__expand_app_registry_value.sql";

    @Test
    void whenApplyRegistryValueExpansionThenPreserveRowsAndAcceptLongValues() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:registry-value-expansion;shutdown=true", "SA", "")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE PUBLIC.APP_REGISTRY (
                            REGISTRY_NAME VARCHAR(255) PRIMARY KEY,
                            VALUE VARCHAR(255)
                        )
                        """);
                statement.executeUpdate("""
                        INSERT INTO PUBLIC.APP_REGISTRY (REGISTRY_NAME, VALUE)
                        VALUES ('INSTALLED', 'true')
                        """);
            }

            ScriptUtils.executeSqlScript(connection, new EncodedResource(new ClassPathResource(MIGRATION)));

            String largeSettings = "x".repeat(5_000);
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO PUBLIC.APP_REGISTRY (REGISTRY_NAME, VALUE)
                    VALUES (?, ?)
                    """)) {
                statement.setString(1, "plugin.apinity.global-execution-settings");
                statement.setString(2, largeSettings);
                assertEquals(1, statement.executeUpdate());
            }

            assertEquals("true", readValue(connection, "INSTALLED"));
            assertEquals(largeSettings, readValue(connection, "plugin.apinity.global-execution-settings"));
        }
    }

    private String readValue(Connection connection, String registryKey) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT VALUE
                FROM PUBLIC.APP_REGISTRY
                WHERE REGISTRY_NAME = ?
                """)) {
            statement.setString(1, registryKey);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }
}
