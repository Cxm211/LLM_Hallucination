public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    int csLength = cs.length();
    int searchLength = searchChars.length;
    for (int i = 0; i < csLength; ) {
        int cp = Character.codePointAt(cs, i);
        int charCount = Character.charCount(cp);
        for (int j = 0; j < searchLength; ) {
            int searchCp = Character.codePointAt(searchChars, j);
            int searchCharCount = Character.charCount(searchCp);
            if (searchCp == cp) {
                return true;
            }
            j += searchCharCount;
        }
        i += charCount;
    }
    return false;
}