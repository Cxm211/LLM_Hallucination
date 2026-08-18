    public static int indexOfAnyBut(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int csLen = cs.length();
        int searchLen = searchChars.length;
        outer:
        for (int i = 0; i < csLen; i++) {
            char ch = cs.charAt(i);
            boolean matched = false;
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch)) {
                        if (j + 1 < searchLen) {
                            if (i + 1 < csLen) {
                                if (searchChars[j + 1] == cs.charAt(i + 1)) {
                                    matched = true;
                                    break;
                                }
                            }
                        } else {
                            matched = true;
                            break;
                        }
                    } else {
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }