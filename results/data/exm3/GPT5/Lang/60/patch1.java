public int indexOf(char ch, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (startIndex >= size) {
            return -1;
        }
        char[] thisBuf = buffer;
        int len = size;
        for (int i = startIndex; i < len; i++) {
            if (thisBuf[i] == ch) {
                return i;
            }
        }
        return -1;
    }