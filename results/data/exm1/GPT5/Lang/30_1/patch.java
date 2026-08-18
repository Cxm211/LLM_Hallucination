public static int indexOfAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        int csLast = csLen - 1;
        int searchLast = searchLen - 1;
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    // handle possible surrogate pair match
                    if (ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE) {
                        if (i < csLast && j < searchLast) {
                            if (searchChars[j + 1] == cs.charAt(i + 1)) {
                                return i;
                            }
                        }
                        // if next does not match or no room, treat as single char match
                        return i;
                    }
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }