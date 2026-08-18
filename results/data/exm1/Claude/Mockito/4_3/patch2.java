private String exceptionCauseMessageIfAvailable(Exception details) {
    if (details.getCause() == null) {
        return "";
    }
    return details.getCause().getMessage();
}