public static String getShortClassName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }
        if (className.length() == 0) {
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
            } else if (className.length() == 1) {
                switch (className.charAt(0)) {
                    case 'Z': className = "boolean"; break;
                    case 'B': className = "byte"; break;
                    case 'C': className = "char"; break;
                    case 'S': className = "short"; break;
                    case 'I': className = "int"; break;
                    case 'J': className = "long"; break;
                    case 'F': className = "float"; break;
                    case 'D': className = "double"; break;
                    default: break;
                }
            }
        }

        int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        String out = className.substring(lastDotIdx + 1).replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        if (dim > 0) {
            StringBuilder sb = new StringBuilder(out.length() + 2 * dim);
            sb.append(out);
            for (int i = 0; i < dim; i++) {
                sb.append("[]");
            }
            out = sb.toString();
        }
        return out;
    }