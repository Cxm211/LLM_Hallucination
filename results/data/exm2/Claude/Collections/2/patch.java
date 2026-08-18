public String getInclude() {
        if (include != null && "".equals(include)) {
            return null;
        }
        return include;  // backwards compatability
}