package com.unimelb.swen90007.jspapp.datasource;

import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;

import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    /**
     * How many connections to open.
     */
    private static final int POOL_SIZE = 5;

    /**
     * Singleton instance
     */
    private static ConnectionPool instance;

    /**
     * List holding the pool of connections that are available.
     */
    private final List<DBConnection> availableConnections;

    /**
     * List holding the pool of connections that are being used.
     */
    private final List<DBConnection> usedConnections;

    /**
     * Get the singleton instance of this class. Lazily constructs the
     * instance if it is null.
     *
     * @return The singleton `ConnectionPool` instance.
     */
    public static ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /**
     * Construct a new ConnectionPool with `POOL_SIZE` number of connections.
     */
    private ConnectionPool() {
        availableConnections = new ArrayList<>(POOL_SIZE);
        usedConnections = new ArrayList<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            availableConnections.add(new DBConnection());
        }
    }

    /**
     * Get an available connection from the connection pool.
     *
     * @return An available database connection
     * @throws ConnectionUnavailableException if there is no connection available
     */
    public synchronized DBConnection getConnection() throws ConnectionUnavailableException {
        // Throw error if no connection is available
        if (availableConnections.isEmpty()) {
            throw new ConnectionUnavailableException();
        }

        // Use the last connection in `availableConnections`
        DBConnection connection = availableConnections.remove(
                availableConnections.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    /**
     * Make the given connection available to the connection pool again.
     *
     * @param connection The connection that is free to use again
     */
    public synchronized void releaseConnection(DBConnection connection) {
        if (connection == null) return;
        usedConnections.remove(connection);
        availableConnections.add(connection);
    }

    /**
     * Close the open connections.
     */
    public void close() {
        availableConnections.forEach(DBConnection::close);
        usedConnections.forEach(DBConnection::close);
    }
}
