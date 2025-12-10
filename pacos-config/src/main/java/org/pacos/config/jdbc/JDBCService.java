package org.pacos.config.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public abstract class JDBCService {
    private final DataSource dataSource;

    protected JDBCService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected boolean tableExists(String tableName) {
        try (ResultSet resultSet = executeQuery("SELECT table_name FROM information_schema.tables " +
                "where TABLE_NAME = '"+tableName+"'")) {
            return resultSet.next();
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }
    protected void execute(String query) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.execute(query);
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }

    protected ResultSet executeQuery(String query) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new PacosJDBCException(e);
        }
    }

    protected DataSource getDataSource() {
        return dataSource;
    }


}
