public static int indexOfAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return INDEX_NOT_FOUND;
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
                return i;
            }
            j += searchCharCount;
        }
        i += charCount;
    }
    return INDEX_NOT_FOUND;
}