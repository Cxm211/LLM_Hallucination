public static boolean containsNone(CharSequence cs, char[] searchChars) {
    if (cs == null || searchChars == null) {
        return true;
    }
    int csLen = cs.length();
    int searchLen = searchChars.length;
    for (int i = 0; i < csLen; ) {
        int cp = Character.codePointAt(cs, i);
        int charCount = Character.charCount(cp);
        for (int j = 0; j < searchLen; ) {
            int searchCp = Character.codePointAt(searchChars, j);
            int searchCharCount = Character.charCount(searchCp);
            if (searchCp == cp) {
                return false;
            }
            j += searchCharCount;
        }
        i += charCount;
    }
    return true;
}