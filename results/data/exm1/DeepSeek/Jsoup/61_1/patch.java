    public boolean hasClass(String className) {
        final String classAttr = attributes.get("class");
        if (classAttr.isEmpty()) {
            return false;
        }
        final String[] classes = classAttr.split("\\s+");
        for (String clazz : classes) {
            if (clazz.equalsIgnoreCase(className)) {
                return true;
            }
        }
        return false;
    }