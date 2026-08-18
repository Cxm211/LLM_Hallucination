public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    String csStr = cs.toString();
    String searchStr = new String(searchChars);
    int csLen = csStr.length();
    int charIndex = 0;
    while (charIndex < csLen) {
        int codePoint = Character.codePointAt(csStr, charIndex);
        if (searchStr.indexOf(codePoint) >= 0) {
            return true;
        }
        charIndex += Character.charCount(codePoint);
    }
    return false;
}