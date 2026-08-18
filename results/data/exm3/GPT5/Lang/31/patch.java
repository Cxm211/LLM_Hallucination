public static boolean containsAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return false;
        }
        int csLength = cs.length();
        int searchLength = searchChars.length;
        for (int i = 0; i < csLength; i++) {
            char ch = cs.charAt(i);
            // Try to match a supplementary pair in cs against a supplementary pair in searchChars
            if (Character.isHighSurrogate(ch) && i + 1 < csLength) {
                char ch2 = cs.charAt(i + 1);
                if (Character.isLowSurrogate(ch2)) {
                    for (int j = 0; j < searchLength - 1; j++) {
                        if (searchChars[j] == ch && searchChars[j + 1] == ch2) {
                            return true;
                        }
                    }
                }
            }
            // Match single BMP chars, but avoid falsely matching a surrogate that is part of a pair in searchChars
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    // If this char is a high surrogate and the next search char is a low surrogate,
                    // then this search char is part of a surrogate pair and should not match alone.
                    if (Character.isHighSurrogate(ch)) {
                        if (j + 1 < searchLength && Character.isLowSurrogate(searchChars[j + 1])) {
                            continue;
                        }
                    } else if (Character.isLowSurrogate(ch)) {
                        // If this is a low surrogate and the previous search char is a high surrogate,
                        // then it's part of a pair and should not match alone.
                        if (j > 0 && Character.isHighSurrogate(searchChars[j - 1])) {
                            continue;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }