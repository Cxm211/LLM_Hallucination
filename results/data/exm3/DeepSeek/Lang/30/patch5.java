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
            if (Character.isLowSurrogate(ch) && i > 0 && Character.isHighSurrogate(cs.charAt(i-1))) {
                continue;
            }
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch) && i < csLast && j < searchLast) {
                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
                            return false;
                        }
                    } else {
                        if (Character.isHighSurrogate(ch) && j < searchLast && Character.isLowSurrogate(searchChars[j+1])) {
                            continue;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }