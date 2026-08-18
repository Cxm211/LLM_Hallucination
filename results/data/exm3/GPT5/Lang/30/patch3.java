public static int indexOfAnyBut(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        final int csLen = cs.length();
        final int searchLen = searchChars.length;
        outer:
        for (int i = 0; i < csLen; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch)) {
                        if (i + 1 < csLen) {
                            if (j + 1 < searchLen && searchChars[j + 1] == cs.charAt(i + 1)) {
                                // full surrogate pair is in the search set, skip the low surrogate unit
                                i++;
                                continue outer;
                            }
                            // only high surrogate matched but pair did not: treat as not in set -> return i
                            return i;
                        }
                        // high surrogate at end without pair: treat as not in set -> return i
                        return i;
                    } else {
                        // BMP char is in the search set
                        continue outer;
                    }
                }
            }
            return i;
        }
        return INDEX_NOT_FOUND;
    }