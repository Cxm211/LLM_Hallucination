public static String getPackageName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }

        int arrayDims = 0;
        while (className.startsWith("[")) {
            arrayDims++;
            className = className.substring(1);
        }
        if (className.startsWith("L") && className.endsWith(";")) {
            className = className.substring(1, className.length() - 1);
        } else if (arrayDims > 0 && className.length() == 1) {
            return StringUtils.EMPTY;
        }

        int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (i == -1) {
            return StringUtils.EMPTY;
        }
        return className.substring(0, i);
    }