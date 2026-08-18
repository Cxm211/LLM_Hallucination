public boolean contains(char ch) {
        char[] thisBuf = buffer;
        int len = size;
        for (int i = 0; i < len; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }