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
                    if (Character.isHighSurrogate(ch)) {
                        if (i < csLast && j < searchLast) {
                            char nextCs = cs.charAt(i + 1);
                            char nextSearch = searchChars[j + 1];
                            if (Character.isLowSurrogate(nextCs) && Character.isLowSurrogate(nextSearch) && nextSearch == nextCs) {
                                return true;
                            }
                        }
                        // high surrogate match without proper low surrogate pairing should not count
                        continue;
                    }
                    if (Character.isLowSurrogate(ch)) {
                        // if this low surrogate is part of a surrogate pair in searchChars, do not match standalone
                        if (j > 0 && Character.isHighSurrogate(searchChars[j - 1])) {
                            continue;
                        }
                        return true;
                    }
                    // ch is in the Basic Multilingual Plane
                    return true;
                }
            }
        }
        return false;
    }