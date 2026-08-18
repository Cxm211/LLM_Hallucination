public int indexOf(String str, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (str == null) {
            return -1;
        }
        int strLen = str.length();
        if (strLen == 0) {
            return startIndex;
        }
        if (startIndex >= size) {
            return -1;
        }
        if (strLen == 1) {
            return indexOf(str.charAt(0), startIndex);
        }
        if (strLen > size) {
            return -1;
        }
        char[] thisBuf = buffer;
        int max = size - strLen; // last index to start matching
        outer:
        for (int i = startIndex; i <= max; i++) {
            for (int j = 0; j < strLen; j++) {
                if (str.charAt(j) != thisBuf[i + j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }