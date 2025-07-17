package exception;

/**
 * Represents a standardized error message returned in HTTP responses.
 *
 * @param message the error message describing what went wrong
 */
public record ErrorMessage(String message) {
}
