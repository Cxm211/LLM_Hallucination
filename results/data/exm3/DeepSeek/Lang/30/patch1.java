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
            if (Character.isLowSurrogate(ch) && i > 0 && Character.isHighSurrogate(cs.charAt(i-1))) {
                continue;
            }
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch) && i < csLast && j < searchLast) {
                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
                            return true;
                        }
                    } else {
                        if (Character.isHighSurrogate(ch) && j < searchLast && Character.isLowSurrogate(searchChars[j+1])) {
                            continue;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }