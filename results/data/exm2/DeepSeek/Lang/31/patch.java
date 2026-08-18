public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    int csLen = cs.length();
    int searchLen = searchChars.length;
    for (int i = 0; i < csLen; ) {
        int codePointI = Character.codePointAt(cs, i);
        int charCountI = Character.charCount(codePointI);
        for (int j = 0; j < searchLen; ) {
            int codePointJ;
            int charCountJ;
            if (j + 1 < searchLen && Character.isHighSurrogate(searchChars[j]) && Character.isLowSurrogate(searchChars[j+1])) {
                codePointJ = Character.toCodePoint(searchChars[j], searchChars[j+1]);
                charCountJ = 2;
            } else {
                codePointJ = searchChars[j];
                charCountJ = 1;
            }
            if (codePointI == codePointJ) {
                return true;
            }
            j += charCountJ;
        }
        i += charCountI;
    }
    return false;
}