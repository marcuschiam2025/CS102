package model;

// status is moved to an enum
public enum CaseResultStatus {
    TO_BE_TESTED ("To be tested"),
    COMPLETED ("Completed"),
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