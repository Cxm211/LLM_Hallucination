    public static int indexOfAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch)) {
                        if (j + 1 < searchLen) {
                            if (i + 1 < csLen) {
                                if (searchChars[j + 1] == cs.charAt(i + 1)) {
                                    return i;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            return i;
                        }
                    } else {
                        return i;
                    }
                }
            }
        }
        return INDEX_NOT_FOUND;
    }