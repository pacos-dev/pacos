package org.config;

import java.util.Objects;
import java.util.Properties;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import org.pacos.core.config.database.DatabaseConfiguration;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan(basePackages = { "org.pacos.core" },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = DatabaseConfiguration.class) })
@EnableJpaRepositories(basePackages = { "org.pacos.core" },
        entityManagerFactoryRef = "coreEntityManagerFactory",
        transactionManagerRef = "coreTransactionManager")
@PropertySource({ "classpath:test-core-persistance.properties" })
public class CoreContextConfiguration {

    static {
        System.setProperty("logback.configurationFile", "test-core-persistance.properties");
    }

    @Autowired
    private Environment environment;

    @Bean("coreDataSource")
    public DataSource coreDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty("jdbc.driverClassName")));
        dataSource.setUrl(environment.getProperty("jdbc.url"));
        dataSource.setPassword(environment.getProperty("jdbc.password"));
        dataSource.setUsername(environment.getProperty("jdbc.username"));
        return dataSource;
    }

    @Bean
    public JpaVendorAdapter databaseJpaVendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        return vendorAdapter;
    }

    @Bean("coreEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean coreEntityManagerFactory(DataSource dataSource,
            JpaVendorAdapter databaseJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean container = new LocalContainerEntityManagerFactoryBean();
        container.setPackagesToScan("org.pacos.core");
        container.setDataSource(dataSource);
        container.setJpaVendorAdapter(databaseJpaVendorAdapter);
        Properties jpaProperties = new Properties();
        //Disable logging of all generated sql
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        container.setJpaProperties(jpaProperties);
        return container;
    }

    @Bean("coreTransactionManager")
    public PlatformTransactionManager coreTransactionManager(
            @Qualifier("coreEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean("swaggerUiConfigProperties")
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        return new SwaggerUiConfigProperties();
    }
}
