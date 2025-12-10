package org.pacos.starter;

import java.util.List;

import javax.sql.DataSource;
import org.pacos.config.jdbc.DataSourceLoader;
import org.pacos.config.property.WorkingDir;
import org.pacos.param.ArgumentBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner is responsible for starting pacos app
 */
public class PacosRunner {

    private static final Logger LOG = LoggerFactory.getLogger(org.pacos.starter.PacosRunner.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Coupler Runner...");
        ArgumentBinder.bindArgsToSystemProperty(args);
        ArgumentBinder.bindJavaOptToSystemProperty(System.getenv("JAVA_OPTS"));

        WorkingDir.initialize();
        LogbackConfig.configure(WorkingDir.load());

        DataSource dataSource = DataSourceLoader.load();

        Pacos pacos = new Pacos(List.of(args), dataSource);
        pacos.start();
    }
}
