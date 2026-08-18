public static String getShortClassName(String className) {
    if (className == null) {
        return StringUtils.EMPTY;
    }
    if (className.length() == 0) {
        return StringUtils.EMPTY;
    }

    // Handle array encoding
    int arrayPrefix = 0;
    while (arrayPrefix < className.length() && className.charAt(arrayPrefix) == '[') {
        arrayPrefix++;
    }
    if (arrayPrefix > 0) {
        StringBuilder sb = new StringBuilder();
        String component = className.substring(arrayPrefix);
        if (component.charAt(0) == 'L') {
            component = component.substring(1, component.length() - 1);
            sb.append(getShortClassName(component));
        } else {
            // primitive type
            switch (component.charAt(0)) {
                case 'B': sb.append("byte"); break;
                case 'C': sb.append("char"); break;
                case 'D': sb.append("double"); break;
                case 'F': sb.append("float"); break;
                case 'I': sb.append("int"); break;
                case 'J': sb.append("long"); break;
                case 'S': sb.append("short"); break;
                case 'Z': sb.append("boolean"); break;
                default: sb.append(component); break;
            }
        }
        for (int i = 0; i < arrayPrefix; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
    int innerIdx = className.indexOf(
            INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
    String out = className.substring(lastDotIdx + 1);
    if (innerIdx != -1) {
        out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
    }
    return out;
}