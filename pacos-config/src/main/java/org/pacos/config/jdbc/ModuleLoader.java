package org.pacos.config.jdbc;

import java.nio.file.Path;
import java.util.List;

import javax.sql.DataSource;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;

/**
 * This class loads all dependencies necessary run pacos in installed configuration.
 * Configuration is stored in db
 */

public class ModuleLoader {

    private ModuleLoader() {
    }

    public static List<Path> load() {
        DataSource dataSource = DataSourceLoader.load();
        List<AppArtifact> modules = new ModuleJDBCService(dataSource).loadActiveModules();
        Path libPath = WorkingDir.getLibPath();
        return modules.stream().map(m -> libPath.resolve(m.getJarPath())).toList();
    }

}
