public static boolean containsAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return false;
        }
        int csLength = cs.length();
        int searchLength = searchChars.length;
        int csLast = csLength - 1;
        int searchLast = searchLength - 1;
        for (int i = 0; i < csLength; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    if (ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE) {
                        // ch is a high surrogate; need to check low surrogate
                        if (i < csLast && j < searchLast) {
                            // check if the low surrogate matches
                            if (searchChars[j + 1] == cs.charAt(i + 1)) {
                                return true;
                            }
                        }
                        // else: not a complete pair, so no match; continue
                    } else {
                        // ch is a BMP character
                        return true;
                    }
                }
            }
        }
        return false;
    }