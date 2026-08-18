public static boolean containsNone(CharSequence cs, char[] searchChars) {
    if (cs == null || searchChars == null) {
        return true;
    }
    String csStr = cs.toString();
    String searchStr = new String(searchChars);
    int csLen = csStr.length();
    int charIndex = 0;
    while (charIndex < csLen) {
        int codePoint = Character.codePointAt(csStr, charIndex);
        if (searchStr.indexOf(codePoint) >= 0) {
            return false;
        }
        charIndex += Character.charCount(codePoint);
    }
    return true;
}