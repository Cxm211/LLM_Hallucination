public static int indexOfAnyBut(String str, String searchChars) {
    if (isEmpty(str) || isEmpty(searchChars)) {
        return INDEX_NOT_FOUND;
    }
    int strLen = str.length();
    int charIndex = 0;
    while (charIndex < strLen) {
        int codePoint = str.codePointAt(charIndex);
        if (searchChars.indexOf(codePoint) < 0) {
            return charIndex;
        }
        charIndex += Character.charCount(codePoint);
    }
    return INDEX_NOT_FOUND;
}