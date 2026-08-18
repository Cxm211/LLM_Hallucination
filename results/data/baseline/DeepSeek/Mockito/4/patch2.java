private String exceptionCauseMessageIfAvailable(Exception details) {
    Throwable cause = details.getCause();
    if (cause != null) {
        return cause.getMessage();
    }
    return null;
}