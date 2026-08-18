public static int indexOfAnyBut(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return INDEX_NOT_FOUND;
    }
    String csStr = cs.toString();
    String searchStr = new String(searchChars);
    int csLen = csStr.length();
    int charIndex = 0;
    while (charIndex < csLen) {
        int codePoint = Character.codePointAt(csStr, charIndex);
        if (searchStr.indexOf(codePoint) < 0) {
            return charIndex;
        }
        charIndex += Character.charCount(codePoint);
    }
    return INDEX_NOT_FOUND;
}