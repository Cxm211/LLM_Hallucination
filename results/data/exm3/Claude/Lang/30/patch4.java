public static int indexOfAnyBut(String str, String searchChars) {
    if (isEmpty(str) || isEmpty(searchChars)) {
        return INDEX_NOT_FOUND;
    }
    int strLen = str.length();
    int strLast = strLen - 1;
    for (int i = 0; i < strLen; i++) {
        char ch = str.charAt(i);
        int searchIndex = searchChars.indexOf(ch);
        if (searchIndex < 0) {
            return i;
        }
        if (i < strLast && searchIndex < searchChars.length() - 1 && ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE) {
            // ch is a high surrogate
            if (searchChars.charAt(searchIndex + 1) == str.charAt(i + 1)) {
                i++;
            } else {
                return i;
            }
        }
    }
    return INDEX_NOT_FOUND;
}