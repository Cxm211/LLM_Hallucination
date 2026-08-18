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
            String remaining = className.substring(dims);
            String componentName;
            if (remaining.charAt(0) == 'L') {
                // Strip Object type encoding
                componentName = remaining.substring(1, remaining.length() - 1);
            } else {
                char code = remaining.charAt(0);
                switch (code) {
                    case 'Z': componentName = "boolean"; break;
                    case 'B': componentName = "byte"; break;
                    case 'C': componentName = "char"; break;
                    case 'S': componentName = "short"; break;
                    case 'I': componentName = "int"; break;
                    case 'J': componentName = "long"; break;
                    case 'F': componentName = "float"; break;
                    case 'D': componentName = "double"; break;
                    default: componentName = String.valueOf(code);
                }
            }
            String shortComponentName = getShortClassName(componentName);
            StringBuilder result = new StringBuilder(shortComponentName);
            for (int i = 0; i < dims; i++) {
                result.append("[]");
            }
            return result.toString();
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