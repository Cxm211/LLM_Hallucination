private String exceptionCauseMessageIfAvailable(Exception details) {
        if (details == null) {
            return null;
        }
        Throwable cause = details.getCause();
        return cause != null ? cause.getMessage() : null;
    }