public String getInclude() {
    if ("".equals(include)) {
        return null;
    }
    return include;  // backwards compatability
}