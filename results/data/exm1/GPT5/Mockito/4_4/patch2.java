private String exceptionCauseMessageIfAvailable(Exception details) {
        Throwable cause = details.getCause();
        return cause != null ? cause.getMessage() : null;
    }