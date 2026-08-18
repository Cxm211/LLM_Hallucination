public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    String searchString = new String(searchChars);
    int csLength = cs.length();
    for (int i = 0; i < csLength; ) {
        int cp = Character.codePointAt(cs, i);
        if (searchString.indexOf(cp) >= 0) {
            return true;
        }
        i += Character.charCount(cp);
    }
    return false;
}