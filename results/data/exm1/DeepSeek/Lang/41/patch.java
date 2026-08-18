public static String getShortClassName(String className) {
    if (className == null) {
        return StringUtils.EMPTY;
    }
    if (className.length() == 0) {
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
        } else if ("Z".equals(componentType)) {
            componentType = "boolean";
        } else if ("B".equals(componentType)) {
            componentType = "byte";
        } else if ("C".equals(componentType)) {
            componentType = "char";
        } else if ("D".equals(componentType)) {
            componentType = "double";
        } else if ("F".equals(componentType)) {
            componentType = "float";
        } else if ("I".equals(componentType)) {
            componentType = "int";
        } else if ("J".equals(componentType)) {
            componentType = "long";
        } else if ("S".equals(componentType)) {
            componentType = "short";
        }
        String shortName = getShortClassName(componentType);
        StringBuilder sb = new StringBuilder(shortName);
        for (int i = 0; i < dims; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    // Strip Object type encoding
    int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
    int innerIdx = className.indexOf(
            INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
    String out = className.substring(lastDotIdx + 1);
    if (innerIdx != -1) {
        out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
    }
    return out;
}