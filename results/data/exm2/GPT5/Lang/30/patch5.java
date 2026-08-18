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
                    if (ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE) {
                        if (i < csLast && j < searchLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                            return false;
                        } else {
                            // only half of a surrogate matched; ignore and continue searching
                            continue;
                        }
                    } else {
                        // ch is in the Basic Multilingual Plane
                        return false;
                    }
                }
            }
        }
        return true;
    }