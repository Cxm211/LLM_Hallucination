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
            String remaining = className.substring(dims);
            if (remaining.charAt(0) == 'L') {
                // Strip Object type encoding
                String componentName = remaining.substring(1, remaining.length() - 1);
                return getPackageName(componentName);
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