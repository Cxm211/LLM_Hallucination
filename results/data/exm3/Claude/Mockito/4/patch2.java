private String exceptionCauseMessageIfAvailable(Exception details) {
    return details.getCause().getMessage();
}

private String getMockName(Object mock) {
    try {
        return String.valueOf(mock);
    } catch (Exception e) {
        return "<mock>";
    }
}