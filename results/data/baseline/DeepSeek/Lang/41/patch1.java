public static String getPackageName(String className) {
    if (className == null) {
        return StringUtils.EMPTY;
    }

    // Strip array encoding
    // Strip Object type encoding
    int arrayPrefix = 0;
    while (arrayPrefix < className.length() && className.charAt(arrayPrefix) == '[') {
        arrayPrefix++;
    }
    if (arrayPrefix > 0) {
        className = className.substring(arrayPrefix);
        if (className.charAt(0) == 'L') {
            className = className.substring(1, className.length() - 1);
        } else {
            return StringUtils.EMPTY;
        }
    }

    int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
    if (i == -1) {
        return StringUtils.EMPTY;
    }
    return className.substring(0, i);
}