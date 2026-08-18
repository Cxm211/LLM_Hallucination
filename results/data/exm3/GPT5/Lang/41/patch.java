public static String getShortClassName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }
        if (className.length() == 0) {
            return StringUtils.EMPTY;
        }

        StringBuilder arraySuffix = new StringBuilder();
        // Handle array encoding
        while (className.startsWith("[")) {
            className = className.substring(1);
            arraySuffix.append("[]");
        }
        // Strip Object type encoding
        if (className.startsWith("L") && className.endsWith(";")) {
            className = className.substring(1, className.length() - 1);
        } else if (className.length() == 1) {
            // Primitive type encoding in arrays or direct encodings
            String primitive;
            switch (className.charAt(0)) {
                case 'Z': primitive = "boolean"; break;
                case 'B': primitive = "byte"; break;
                case 'C': primitive = "char"; break;
                case 'D': primitive = "double"; break;
                case 'F': primitive = "float"; break;
                case 'I': primitive = "int"; break;
                case 'J': primitive = "long"; break;
                case 'S': primitive = "short"; break;
                default: primitive = null; break;
            }
            if (primitive != null) {
                return primitive + arraySuffix.toString();
            }
        }

        int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        int innerIdx = className.indexOf(
                INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
        String out = className.substring(lastDotIdx + 1);
        if (innerIdx != -1) {
            out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        }
        return out + arraySuffix.toString();
    }