package dataaccess;

/**
 * Custom exception for errors related to data access operations.
 */
public class DataAccessException extends Exception {

    /**
     * Constructs a new DataAccessException with the specified detail message.
     *
     * @param message the detail message describing the error
     */
    public DataAccessException(String message) {
        super(message);
    }
}
