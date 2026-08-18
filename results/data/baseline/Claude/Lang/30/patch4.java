public static int indexOfAnyBut(String str, String searchChars) {
    if (isEmpty(str) || isEmpty(searchChars)) {
        return INDEX_NOT_FOUND;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
        char ch = str.charAt(i);
        boolean chHighSurrogate = ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE;
        if (chHighSurrogate && i < strLen - 1) {
            char ch2 = str.charAt(i + 1);
            if (searchChars.indexOf(ch) < 0 || searchChars.indexOf(ch2) < 0) {
                return i;
            }
        } else {
            if (searchChars.indexOf(ch) < 0) {
                return i;
            }
        }
    }
    return INDEX_NOT_FOUND;
}