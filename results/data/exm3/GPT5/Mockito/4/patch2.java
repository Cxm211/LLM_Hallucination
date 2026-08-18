private String exceptionCauseMessageIfAvailable(Exception details) {
        Throwable cause = details != null ? details.getCause() : null;
        String message = cause != null ? cause.getMessage() : (details != null ? details.getMessage() : null);
        return message;
    }