public StrBuilder appendFixedWidthPadRight(Object obj, int width, char padChar) {
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
                str.getChars(0, width, buffer, size);
            } else {
                int padLen = width - strLen;
                str.getChars(0, strLen, buffer, size);
                for (int i = 0; i < padLen; i++) {
                    buffer[size + strLen + i] = padChar;
                }
            }
            size += width;
        }
        return this;
    }