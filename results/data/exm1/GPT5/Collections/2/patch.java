public String getInclude() {
        if (include == null) {
            return "include";  // default for backwards compatibility
        }
        return include.length() == 0 ? null : include;
    }