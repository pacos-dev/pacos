package org.pacos.config.jdbc;

import org.pacos.config.repository.data.AppArtifact;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArtifactJDBC extends JDBCService {

    protected AbstractArtifactJDBC(DataSource dataSource) {
        super(dataSource);
    }


    abstract String getTableName();

    public boolean tableExists() {
        return super.tableExists(getTableName());
    }

    List<AppArtifact> getAppArtifacts(String sql) {
        try (ResultSet resultSet = executeQuery(sql)) {
            List<AppArtifact> modules = new ArrayList<>();
            while (resultSet.next()) {
                modules.add(mapLibraryFromResultSet(resultSet));
            }
            return modules;
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }

    AppArtifact mapLibraryFromResultSet(ResultSet resultSet) throws SQLException {
        String group = resultSet.getString("group_id");
        String artifactName = resultSet.getString("artifact_name");
        String version = resultSet.getString("version");
        return new AppArtifact(group,artifactName,version);
    }

}
