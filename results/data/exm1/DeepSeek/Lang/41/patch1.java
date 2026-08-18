public static String getPackageName(String className) {
    if (className == null) {
        return StringUtils.EMPTY;
    }

    // Handle array encoding
    int dims = 0;
    while (dims < className.length() && className.charAt(dims) == '[') {
        dims++;
    }
    if (dims > 0) {
        String componentType = className.substring(dims);
        if (componentType.startsWith("L") && componentType.endsWith(";")) {
            componentType = componentType.substring(1, componentType.length() - 1);
        } else {
            return StringUtils.EMPTY;
        }
        return getPackageName(componentType);
    }

    // Strip Object type encoding
    int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
    if (i == -1) {
        return StringUtils.EMPTY;
    }
    return className.substring(0, i);
}