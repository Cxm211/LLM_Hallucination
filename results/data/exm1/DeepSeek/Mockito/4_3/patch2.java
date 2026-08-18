private String exceptionCauseMessageIfAvailable(Exception details) {
        Throwable cause = details.getCause();
        return cause == null ? null : cause.getMessage();
    }