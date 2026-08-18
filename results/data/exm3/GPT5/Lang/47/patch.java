public StrBuilder appendFixedWidthPadLeft(Object obj, int width, char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String nt = getNullText();
            String str;
            if (obj == null) {
                str = (nt == null ? "" : nt);
            } else {
                String s = obj.toString();
                str = (s == null ? (nt == null ? "" : nt) : s);
            }
            int strLen = str.length();
            if (strLen >= width) {
                str.getChars(strLen - width, strLen, buffer, size);
            } else {
                int padLen = width - strLen;
                for (int i = 0; i < padLen; i++) {
                    buffer[size + i] = padChar;
                }
                str.getChars(0, strLen, buffer, size + padLen);
            }
            size += width;
        }
        return this;
    }