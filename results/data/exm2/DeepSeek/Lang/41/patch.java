public static String getShortClassName(String className) {
    if (className == null) {
        return StringUtils.EMPTY;
    }
    if (className.length() == 0) {
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
            char c = base.charAt(0);
            switch (c) {
                case 'Z': baseName = "boolean"; break;
                case 'B': baseName = "byte"; break;
                case 'C': baseName = "char"; break;
                case 'D': baseName = "double"; break;
                case 'F': baseName = "float"; break;
                case 'I': baseName = "int"; break;
                case 'J': baseName = "long"; break;
                case 'S': baseName = "short"; break;
                case 'V': baseName = "void"; break;
                default: baseName = base;
            }
        } else {
            baseName = base;
        }
    }

    int lastDotIdx = baseName.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
    int innerIdx = baseName.indexOf(
            INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
    String out = baseName.substring(lastDotIdx + 1);
    if (innerIdx != -1) {
        out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
    }

    if (dims > 0) {
        StringBuilder sb = new StringBuilder(out);
        for (int i = 0; i < dims; i++) {
            sb.append("[]");
        }
        out = sb.toString();
    }
    return out;
}