public static boolean containsNone(CharSequence cs, char[] searchChars) {
        if (cs == null || searchChars == null) {
            return true;
        }
        final int csLen = cs.length();
        final int searchLen = searchChars.length;
        for (int i = 0; i < csLen; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch)) {
                        if (i + 1 < csLen) {
                            if (j + 1 < searchLen && searchChars[j + 1] == cs.charAt(i + 1)) {
                                return false;
                            }
                            // not a full surrogate pair match: keep searching
                        }
                        // high surrogate at end or not matching pair: keep searching
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }