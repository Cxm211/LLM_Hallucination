private String exceptionCauseMessageIfAvailable(Exception details) {
        if (details == null) {
            return "";
        }
        Throwable cause = details.getCause();
        return (cause != null && cause.getMessage() != null) ? cause.getMessage() : "";
    }