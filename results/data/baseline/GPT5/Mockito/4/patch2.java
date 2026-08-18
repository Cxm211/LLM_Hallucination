private String exceptionCauseMessageIfAvailable(Exception details) {
        if (details == null) {
            return null;
        }
        Throwable cause = details.getCause();
        if (cause != null && cause.getMessage() != null) {
            return cause.getMessage();
        }
        return details.getMessage();
    }