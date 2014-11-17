package com.google.sitebricks.error;

public interface ErrorHandler {

    /**
     * Handles errors that are thrown by the server during the request processing.
     * Note that unhandled exceptions are rethrown as RuntimeExceptions.
     */
    void handleException(Exception ex) throws RuntimeException;
}
