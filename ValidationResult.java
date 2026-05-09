public class ValidationResult {
    private boolean valid; // if the world valid 
    private String message; // if its wromg or right send a message

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
