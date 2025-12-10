package org.pacos.core.config.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManagerFactory;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = {"org.pacos.core.component.*.repository"},
        entityManagerFactoryRef = "coreEntityManagerFactory",
        transactionManagerRef = "coreTransactionManager")
@EntityScan(basePackages = {"org.pacos.core.component.*.domain"})
@EnableTransactionManagement
public class DatabaseConfiguration {

    private final Environment environment;

    @Autowired
    public DatabaseConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean("coreEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean coreEntityManagerFactory(JpaVendorAdapter coreJpaVendorAdapter,
                                                                           DataSource coreDataSource) {
        LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setDataSource(coreDataSource);
        emFactory.setPackagesToScan("org.pacos.core");
        emFactory.setJpaVendorAdapter(coreJpaVendorAdapter);
        emFactory.setPersistenceUnitName("coreModule");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.show_sql", environment.getProperty("spring.jpa.hibernate.show-sql"));
        properties.put("hibernate.format_sql", environment.getProperty("spring.jpa.hibernate.format-sql"));
        properties.put("hibernate.jdbc.batch_size", environment.getProperty("spring.jpa.hibernate.jdbc.batch_size"));
        properties.put("hibernate.generate_statistics", environment.getProperty("spring.jpa.hibernate.generate_statistics"));
        properties.put("hibernate.transaction.coordinator_class", "jdbc");

        emFactory.setJpaPropertyMap(properties);

        return emFactory;
    }

    @Bean("coreJpaVendorAdapter")
    @Primary
    public JpaVendorAdapter coreJpaVendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        return vendorAdapter;
    }

    @Bean("coreTransactionManager")
    @Primary
    public PlatformTransactionManager coreTransactionManager(
            @Qualifier("coreEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean("coreDataSource")
    @Primary
    public DataSource coreDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty("spring.datasource.driverClassName")));
        dataSource.setJdbcUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));

        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setConnectionTimeout(30000);
        return dataSource;
    }

}
