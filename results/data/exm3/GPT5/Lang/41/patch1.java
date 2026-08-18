public static String getPackageName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }
        if (className.length() == 0) {
            return StringUtils.EMPTY;
        }

        // Strip array encoding
        while (className.startsWith("[")) {
            className = className.substring(1);
        }
        // Strip Object type encoding or handle primitive array encodings
        if (className.startsWith("L") && className.endsWith(";")) {
            className = className.substring(1, className.length() - 1);
        } else if (className.length() == 1) {
            // Primitive arrays (e.g., I, Z, etc.) have no package
            return StringUtils.EMPTY;
        }

        // Primitives have no package
        if ("boolean".equals(className) || "byte".equals(className) ||
                "char".equals(className) || "double".equals(className) ||
                "float".equals(className) || "int".equals(className) ||
                "long".equals(className) || "short".equals(className)) {
            return StringUtils.EMPTY;
        }

        int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (i == -1) {
            return StringUtils.EMPTY;
        }
        return className.substring(0, i);
    }