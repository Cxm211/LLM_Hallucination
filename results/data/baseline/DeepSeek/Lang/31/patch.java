public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    int csLength = cs.length();
    String searchString = new String(searchChars);
    for (int i = 0; i < csLength; ) {
        int cp = Character.codePointAt(cs, i);
        if (searchString.indexOf(cp) >= 0) {
            return true;
        }
        i += Character.charCount(cp);
    }
    return false;
}