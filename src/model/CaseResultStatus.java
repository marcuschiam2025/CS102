package model;

// status is moved to an enum
public enum CaseResultStatus {
    PASSED ("Passed"),
    FAILED ("Failed"),
    TIMEOUT ("Timeout"),
    COMPILE_ERROR ("Compile error"),
    RUNTIME_ERROR ("Runtime error"),
    MISSING_FILE ("Missing file");

    private String message;

    private CaseResultStatus(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return message;
    }
}