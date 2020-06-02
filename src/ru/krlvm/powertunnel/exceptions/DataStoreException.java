package ru.krlvm.powertunnel.exceptions;

/**
 * Exactly says about DataStore load failure
 *
 * @author krlvm
 */
public class DataStoreException extends Exception {

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
