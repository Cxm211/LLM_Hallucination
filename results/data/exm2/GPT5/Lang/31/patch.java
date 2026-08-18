public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    int csLength = cs.length();
    int searchLength = searchChars.length;
    for (int i = 0; i < csLength; i++) {
        char ch = cs.charAt(i);
        for (int j = 0; j < searchLength; j++) {
            if (searchChars[j] == ch) {
                if (Character.isHighSurrogate(ch)) {
                    if (j + 1 < searchLength) {
                        if (i + 1 < csLength && searchChars[j + 1] == cs.charAt(i + 1)) {
                            return true;
                        }
                    }
                    continue;
                } else if (Character.isLowSurrogate(ch)) {
                    continue;
                } else {
                    return true;
                }
            }
        }
    }
    return false;
}