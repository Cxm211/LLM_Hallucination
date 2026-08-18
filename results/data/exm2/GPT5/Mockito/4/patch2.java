private String exceptionCauseMessageIfAvailable(Exception details) {
        if (details == null) return "";
        Throwable cause = details.getCause();
        if (cause == null) return "";
        String msg = cause.getMessage();
        return msg != null ? msg : "";
    }