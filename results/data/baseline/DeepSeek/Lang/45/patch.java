public static String abbreviate(String str, int lower, int upper, String appendToEnd) {
    if (str == null) {
        return null;
    }
    if (str.length() == 0) {
        return StringUtils.EMPTY;
    }
    if (upper == -1 || upper > str.length()) {
        upper = str.length();
    }
    if (upper < lower) {
        upper = lower;
    }
    if (str.length() <= upper) {
        return str;
    }
    int appendLen = StringUtils.defaultString(appendToEnd).length();
    int maxTextLen = upper - appendLen;
    if (maxTextLen < lower) {
        maxTextLen = lower;
    }
    StringBuffer result = new StringBuffer();
    int index = StringUtils.indexOf(str, " ", lower);
    if (index == -1) {
        int end = Math.min(str.length(), maxTextLen);
        result.append(str.substring(0, end));
        result.append(StringUtils.defaultString(appendToEnd));
    } else if (index > maxTextLen) {
        int end = Math.min(str.length(), maxTextLen);
        result.append(str.substring(0, end));
        result.append(StringUtils.defaultString(appendToEnd));
    } else {
        result.append(str.substring(0, index));
        result.append(StringUtils.defaultString(appendToEnd));
    }
    return result.toString();
}