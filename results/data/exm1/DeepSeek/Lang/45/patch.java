public static String abbreviate(String str, int lower, int upper, String appendToEnd) {
    if (str == null) {
        return null;
    }
    if (str.length() == 0) {
        return StringUtils.EMPTY;
    }

    if (lower < 0) {
        lower = 0;
    }
    if (lower > str.length()) {
        lower = str.length();
    }

    if (upper == -1 || upper > str.length()) {
        upper = str.length();
    }
    if (upper < lower) {
        upper = lower;
    }

    StringBuffer result = new StringBuffer();
    int index = StringUtils.lastIndexOf(str, " ", upper);
    if (index == -1 || index < lower) {
        result.append(str.substring(0, upper));
        if (upper != str.length()) {
            result.append(StringUtils.defaultString(appendToEnd));
        }
    } else {
        result.append(str.substring(0, index));
        result.append(StringUtils.defaultString(appendToEnd));
    }
    return result.toString();
}