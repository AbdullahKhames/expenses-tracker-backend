package name.expenses.error;

import java.util.Map;

public class GeneralFailureException extends RuntimeException {
    private final Map<String, String> details;

    public GeneralFailureException(String message) {
        super(message);
        this.details = null;
    }

    public GeneralFailureException(String message, Map<String, String> details) {
        super(message);
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
