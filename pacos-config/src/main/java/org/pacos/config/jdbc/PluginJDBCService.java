package org.pacos.config.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import org.pacos.config.repository.data.AppArtifact;

/**
 * Static access to table APP_PLUGIN
 */
public class PluginJDBCService extends AbstractArtifactJDBC {

    private static final String LOAD_ALL_LIBRARY = """
            SELECT plug.GROUP_ID,plug.ARTIFACT_NAME,plug.VERSION FROM APP_PLUGIN plug
            where plug.REMOVED=FALSE AND plug.DISABLED=FALSE
            UNION ALL
            SELECT dep.GROUP_ID,dep.ARTIFACT_NAME,dep.VERSION FROM APP_PLUGIN plug
                   LEFT JOIN APP_PLUGIN_DEPENDENCY plugdep ON plug.id = plugdep.PLUGIN_ID
                   LEFT JOIN APP_DEPENDENCY dep ON plugdep.DEPENDENCY_ID = dep.ID
            where plug.REMOVED=FALSE AND plug.DISABLED=FALSE AND dep.REMOVED=FALSE
            """;

    public PluginJDBCService(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Return all installed plugins if application is already installed
     */
    public List<AppArtifact> loadPluginsDependencies() {
        if (tableExists()) {
            return getAppArtifacts(LOAD_ALL_LIBRARY);
        }
        return new ArrayList<>();
    }

    @Override
    String getTableName() {
        return "APP_PLUGIN";
    }


}



