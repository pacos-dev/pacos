package org.pacos.config.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;
import org.pacos.config.repository.data.AppArtifact;

public class ModuleJDBCService extends AbstractArtifactJDBC {

    private static final String REMOVE_REMOVED_MODULES = "UPDATE APP_MODULE SET REMOVED=true";
    private static final String INSERT_MODULE = "INSERT INTO APP_MODULE (ID,REMOVED,GROUP_ID,ARTIFACT_NAME" +
            ",VERSION) VALUES (?,?,?,?,?)";

    private static final String SELECT_ENGINE = "SELECT * FROM APP_MODULE WHERE REMOVED=FALSE AND GROUP_ID='org.pacos' AND ARTIFACT_NAME='engine'";

    private static final String SELECT_ACTIVE_MODULES = "SELECT * FROM APP_MODULE WHERE REMOVED=FALSE AND GROUP_ID='org.pacos' AND ARTIFACT_NAME!='engine'";

    public ModuleJDBCService(DataSource dataSource) {
        super(dataSource);
    }

    public AppArtifact loadEngineModule() {
        try (ResultSet resultSet = executeQuery(SELECT_ENGINE)) {
            if (resultSet.next()) {
                return mapLibraryFromResultSet(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }

    public List<AppArtifact> loadActiveModules() {
        return getAppArtifacts(SELECT_ACTIVE_MODULES);
    }

    public void saveModuleConfiguration(List<AppArtifact> modules) {
        execute(REMOVE_REMOVED_MODULES);

        try (Connection connection = getDataSource().getConnection()) {
            saveModuleConfiguration(modules, connection);

        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }

    @Override
    String getTableName() {
        return "APP_MODULE";
    }


    private static void saveModuleConfiguration(List<AppArtifact> modules, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MODULE)) {
            connection.setAutoCommit(false);
            for (AppArtifact m : modules) {
                storeModule(m, preparedStatement);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }

    public void saveSystemVersion(String version) {
        if (getInstalledSystemVersion() == null) {
            execute("INSERT INTO APP_REGISTRY(REGISTRY_NAME,VALUE) VALUES ('SYSTEM_VERSION','" + version + "')");
        } else {
            execute("UPDATE APP_REGISTRY SET VALUE='" + version + "' WHERE REGISTRY_NAME='SYSTEM_VERSION'");
        }

    }

    public String getInstalledSystemVersion() {
        String query = "SELECT VALUE FROM APP_REGISTRY WHERE REGISTRY_NAME='SYSTEM_VERSION'";
        try (ResultSet resultSet = executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    private static void storeModule(AppArtifact m, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, UUID.randomUUID().toString());
        preparedStatement.setBoolean(2, false);
        preparedStatement.setString(3, m.groupId());
        preparedStatement.setString(4, m.artifactName());
        preparedStatement.setString(5, m.version());
        preparedStatement.execute();
    }


}



