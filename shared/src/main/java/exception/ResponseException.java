package exception;

/**
 * Custom exception used to represent HTTP response errors.
 * Includes an HTTP status code and a descriptive error message.
 */
public class ResponseException extends Exception {
    private final int statusCode;

    /**
     * Constructs a ResponseException with a status code and message.
     *
     * @param statusCode the HTTP status code to return (e.g., 400, 401, 403)
     * @param message    the error message to include in the response
     */
    public ResponseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * @return the HTTP status code associated with this exception
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the error message associated with this exception
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
