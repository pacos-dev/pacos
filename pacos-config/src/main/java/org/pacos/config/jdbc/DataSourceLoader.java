package org.pacos.config.jdbc;

import java.util.Properties;

import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.pacos.config.property.ApplicationProperties;

/**
 * Static creator of pacos data source.
 * Used be starter app to check if application is installed
 */
public class DataSourceLoader {

    private DataSourceLoader() {

    }

    public static DataSource load() {
        Properties properties = ApplicationProperties.get();
        JDBCDataSource dataSource = new JDBCDataSource();

        String url = properties.getProperty("spring.datasource.url");

        if (url.contains("workingDir")) {
            String workingDir = System.getProperty("workingDir");
            url = url.replace("${workingDir}", workingDir);
        }

        dataSource.setDatabase(url);
        dataSource.setUser(properties.getProperty("spring.datasource.username"));
        dataSource.setPassword(properties.getProperty("spring.datasource.password"));
        return dataSource;
    }
}
