package org.pacos.config.jdbc;

import javax.sql.DataSource;

/**
 * Provides based access to pacos database
 */
public class PacosJdbcService extends JDBCService {

    private static final String INIT_SQL = """
            create table IF NOT EXISTS APP_MODULE
            (
                ID VARCHAR(36) PRIMARY KEY not null,
                REMOVED BOOLEAN default false,
                GROUP_ID VARCHAR(255) not null,
                ARTIFACT_NAME VARCHAR(255) not null,
                VERSION VARCHAR(50) not null,
                        
                REPO_URL VARCHAR(1000)
                );
                        
            create table if not exists APP_REGISTRY
            (
                REGISTRY_NAME VARCHAR(255) not null
                primary key,
                VALUE         VARCHAR(255)
            );
            """;

    public PacosJdbcService(DataSource dataSource) {
        super(dataSource);
    }

    public void initDatabase() {
        if (!tableExists("APP_MODULE") || !tableExists("APP_REGISTRY")) {
            execute(INIT_SQL);
        }
    }
}
