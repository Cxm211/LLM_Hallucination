public static int indexOfAnyBut(String str, String searchChars) {
    if (isEmpty(str) || isEmpty(searchChars)) {
        return INDEX_NOT_FOUND;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; ) {
        int cp = str.codePointAt(i);
        int charCount = Character.charCount(cp);
        if (searchChars.indexOf(cp) < 0) {
            return i;
        }
        i += charCount;
    }
    return INDEX_NOT_FOUND;
}