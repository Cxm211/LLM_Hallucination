public static boolean containsAny(CharSequence cs, char[] searchChars) {
    if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
        return false;
    }
    java.util.HashSet<Integer> set = new java.util.HashSet<>();
    for (int i = 0; i < searchChars.length; i++) {
        char c = searchChars[i];
        if (Character.isHighSurrogate(c) && i + 1 < searchChars.length) {
            char c2 = searchChars[i + 1];
            if (Character.isLowSurrogate(c2)) {
                set.add(Character.toCodePoint(c, c2));
                i++;
                continue;
            }
        }
        set.add((int) c);
    }
    int csLength = cs.length();
    for (int i = 0; i < csLength; ) {
        int codePoint = Character.codePointAt(cs, i);
        if (set.contains(codePoint)) {
            return true;
        }
        i += Character.charCount(codePoint);
    }
    return false;
}