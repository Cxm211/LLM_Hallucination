public static String getShortClassName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }
        if (className.length() == 0) {
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
            switch (className.charAt(0)) {
                case 'Z': className = "boolean"; break;
                case 'B': className = "byte"; break;
                case 'C': className = "char"; break;
                case 'D': className = "double"; break;
                case 'F': className = "float"; break;
                case 'I': className = "int"; break;
                case 'J': className = "long"; break;
                case 'S': className = "short"; break;
                default: break;
            }
        }

        int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        String out = className.substring(lastDotIdx + 1);
        out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        if (arrayDims > 0) {
            StringBuilder sb = new StringBuilder(out);
            for (int i = 0; i < arrayDims; i++) {
                sb.append("[]");
            }
            out = sb.toString();
        }
        return out;
    }