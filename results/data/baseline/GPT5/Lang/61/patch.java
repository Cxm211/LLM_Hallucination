public int indexOf(String str, int startIndex) {
        if (str == null) {
            return -1;
        }
        startIndex = (startIndex < 0 ? 0 : startIndex);
        int strLen = str.length();
        if (strLen == 0) {
            return startIndex > size ? size : startIndex;
        }
        if (startIndex >= size) {
            return -1;
        }
        if (strLen == 1) {
            return indexOf(str.charAt(0), startIndex);
        }
        if (strLen > size - startIndex) {
            return -1;
        }
        char[] thisBuf = buffer;
        int end = size - strLen;
        outer:
        for (int i = startIndex; i <= end; i++) {
            for (int j = 0; j < strLen; j++) {
                if (str.charAt(j) != thisBuf[i + j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }