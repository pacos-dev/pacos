package org.pacos.core.config.database;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FlywayCoreConfiguration {

    @Value("${spring.datasource.url}")
    public String dbUrl;

    @Value("${spring.datasource.username}")
    public String dbUser;

    @Value("${spring.datasource.password}")
    public String dbPassword;

    @Bean("coreFlyWayMigration")
    @Primary
    public Flyway flyWayMigration(@Qualifier("coreDataSource") DataSource dataSource) {
        Flyway flyway =
                Flyway.configure()
                        .dataSource(dataSource)
                        .baselineOnMigrate(true)
                        .outOfOrder(true)
                        .locations("classpath:db/migration/core")
                        .ignoreMigrationPatterns("*:ignored")
                        .load();

        flyway.migrate();

        return flyway;
    }
}
