package com.unimelb.swen90007.jspapp.datasource;

import org.apache.logging.log4j.LogManager;

import java.sql.*;

/**
 * DBController is a singleton containing the database connection object and
 * credentials.
 */
public class DBConnection {
    private java.sql.Connection connection;

    /**
     * Construct a new DBController. It is made private to avoid construction
     * beside the singleton.
     */
    DBConnection() {
        // Database connection parameters
        String url = "jdbc:postgresql://dpg-cren23bgbbvc73bs8oag-a.oregon-postgres.render.com/mydb_lur2";
        String user = "genshin";
        String password = "1vCsYWUWIJOH1M3AMnAwuRs93qI9AulY";
        // String url = "jdbc:postgresql://localhost:5432/swen90007";
        // String user = "postgres";
        // String password = "password";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LogManager.getLogger().error(
                    "Unable to find org.postgresql.Driver", e);
            return;
        }

        try {
            // Establish the connection
            connection = DriverManager.getConnection(url, user, password);

            if (connection != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            LogManager.getLogger().error("Unable to connect to database", e);
        }
    }

    /**
     * Close the database connection and cleanup resources.
     */
    public void close() {
        // Close the database connection
        try {
            connection.close();
        } catch (SQLException e) {
            LogManager.getLogger().error(
                    "Unable to close database connection", e);
        }
    }

    /**
     * Executes an SQL update statement with the given parameters.
     *
     * @param sql    the SQL statement to execute
     * @param params the parameters for the SQL statement
     * @throws SQLException if a database access error occurs
     */
    public void update(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Executes an SQL query and returns the result set.
     *
     * @param sql    the SQL query to execute
     * @param params the parameters for the SQL query
     * @return the result set of the query
     * @throws SQLException if a database access error occurs
     */
    public ResultSet execute(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    public void commit() throws SQLException {
        connection.commit();
    }
}