public String getInclude() {
        if (include == null) {
            return "include";  // default when never explicitly set
        }
        return include.length() == 0 ? null : include;
    }