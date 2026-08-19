/**
 * Represents an input error that Snoopy can explain to the user.
 */
public class SnoopyException extends Exception {
    /**
     * Creates a Snoopy-specific exception with a user-friendly message.
     *
     * @param message explanation of the invalid input
     */
    public SnoopyException(String message) {
        super(message);
    }
}
