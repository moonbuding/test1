package com.unimelb.swen90007.jspapp.util;

/**
 * Exception thrown when the connection pool runs out of available connection.
 */
public class ConnectionUnavailableException extends Exception {

    /**
     * Construct a new ConnectionUnavailableException.
     */
    public ConnectionUnavailableException() {
    }
}
