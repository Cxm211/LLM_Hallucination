public static String getPackageName(String className) {
    if (className == null) {
        return StringUtils.EMPTY;
    }

    int dims = 0;
    while (dims < className.length() && className.charAt(dims) == '[') {
        dims++;
    }
    String base = className.substring(dims);
    String baseName;
    if (base.startsWith("L") && base.endsWith(";")) {
        baseName = base.substring(1, base.length() - 1);
    } else {
        if (base.length() == 1) {
            return StringUtils.EMPTY;
        } else {
            baseName = base;
        }
    }

    int i = baseName.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
    if (i == -1) {
        return StringUtils.EMPTY;
    }
    return baseName.substring(0, i);
}