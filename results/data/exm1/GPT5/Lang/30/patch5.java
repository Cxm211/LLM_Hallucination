public static boolean containsNone(CharSequence cs, char[] searchChars) {
        if (cs == null || searchChars == null) {
            return true;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        int csLast = csLen - 1;
        int searchLast = searchLen - 1;
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch)) {
                        if (i < csLast && j < searchLast) {
                            char nextCs = cs.charAt(i + 1);
                            char nextSearch = searchChars[j + 1];
                            if (Character.isLowSurrogate(nextCs) && Character.isLowSurrogate(nextSearch) && nextSearch == nextCs) {
                                return false;
                            }
                        }
                        // high surrogate without proper low surrogate pairing in search should not count as contained
                        continue;
                    }
                    if (Character.isLowSurrogate(ch)) {
                        // if low surrogate corresponds to part of a surrogate pair in search, don't count standalone
                        if (j > 0 && Character.isHighSurrogate(searchChars[j - 1])) {
                            continue;
                        }
                        return false;
                    }
                    // BMP match
                    return false;
                }
            }
        }
        return true;
    }