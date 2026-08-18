    public static int indexOfAnyBut(String str, String searchChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        int strLen = str.length();
        int searchLen = searchChars.length();
        int strLast = strLen - 1;
        int searchLast = searchLen - 1;
        outer:
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);
            if (Character.isLowSurrogate(ch) && i > 0 && Character.isHighSurrogate(str.charAt(i-1))) {
                continue;
            }
            for (int j = 0; j < searchLen; j++) {
                if (searchChars.charAt(j) == ch) {
                    if (Character.isHighSurrogate(ch) && i < strLast && j < searchLast) {
                        if (searchChars.charAt(j + 1) == str.charAt(i + 1)) {
                            i++;
                            continue outer;
                        }
                        continue outer;
                    } else {
                        if (Character.isHighSurrogate(ch) && j < searchLast && Character.isLowSurrogate(searchChars.charAt(j+1))) {
                            continue outer;
                        }
                        continue outer;
                    }
                }
            }
            return i;
        }
        return INDEX_NOT_FOUND;
    }