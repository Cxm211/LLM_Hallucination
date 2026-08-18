public static String getPackageName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }

        int dim = 0;
        while (className.length() > 0 && className.charAt(0) == '[') {
            dim++;
            className = className.substring(1);
        }

        if (dim > 0) {
            if (className.length() > 0 && className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
                className = className.substring(1, className.length() - 1);
            } else {
                // primitive arrays have no package
                return StringUtils.EMPTY;
            }
        }

        int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (i == -1) {
            return StringUtils.EMPTY;
        }
        return className.substring(0, i);
    }