    private String exceptionCauseMessageIfAvailable(Exception details) {
        if (details != null && details.getCause() != null) {
            return details.getCause().getMessage();
        }
        return "";
    }