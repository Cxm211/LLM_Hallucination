public int indexOf(char ch, int startIndex) {
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (startIndex >= size) {
            return -1;
        }
        char[] thisBuf = buffer;
        for (int i = startIndex; i < size; i++) {
            if (thisBuf[i] == ch) {
                return i;
            }
        }
        return -1;
    }