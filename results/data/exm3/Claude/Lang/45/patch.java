public static String abbreviate(String str, int lower, int upper, String appendToEnd) {
    // initial parameter checks
    if (str == null) {
        return null;
    }
    if (str.length() == 0) {
        return StringUtils.EMPTY;
    }

    // if the lower value is greater than the length of the string,
    // set to the length of the string
    // if the upper value is -1 (i.e. no limit) or is greater
    // than the length of the string, set to the length of the string
    if (upper == -1 || upper > str.length()) {
        upper = str.length();
    }
    // if upper is less than lower, raise it to lower
    if (upper < lower) {
        upper = lower;
    }

    StringBuffer result = new StringBuffer();
    int index = StringUtils.indexOf(str, " ", lower);
    if (index == -1) {
        result.append(str.substring(0, upper));
        // only if abbreviation has occured do we append the appendToEnd value
        if (upper != str.length()) {
            result.append(StringUtils.defaultString(appendToEnd));
        }
    } else if (index > upper) {
        result.append(str.substring(0, upper));
        result.append(StringUtils.defaultString(appendToEnd));
    } else {
        int endIndex = index;
        // Skip consecutive spaces to find the actual word boundary
        while (endIndex < str.length() && str.charAt(endIndex) == ' ') {
            endIndex++;
        }
        // If we've gone past upper, use upper instead
        if (endIndex > upper) {
            result.append(str.substring(0, upper));
        } else {
            result.append(str.substring(0, index));
        }
        result.append(StringUtils.defaultString(appendToEnd));
    }
    return result.toString();
}