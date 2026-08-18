public static String abbreviate(String str, int lower, int upper, String appendToEnd) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return StringUtils.EMPTY;
        }

        int len = str.length();
        if (lower < 0) {
            lower = 0;
        }
        if (lower > len) {
            lower = len;
        }
        if (upper == -1 || upper > len) {
            upper = len;
        }
        if (upper < lower) {
            upper = lower;
        }
        if (upper > len) {
            upper = len;
        }

        StringBuffer result = new StringBuffer();
        int index = StringUtils.indexOf(str, " ", lower);
        if (index == -1) {
            result.append(str.substring(0, upper));
            if (upper != str.length()) {
                result.append(StringUtils.defaultString(appendToEnd));
            }
        } else if (index > upper) {
            result.append(str.substring(0, upper));
            result.append(StringUtils.defaultString(appendToEnd));
        } else {
            result.append(str.substring(0, index));
            result.append(StringUtils.defaultString(appendToEnd));
        }
        return result.toString();
    }