// ===== FIXED org.apache.commons.lang.text.StrBuilder :: appendFixedWidthPadLeft(Object, int, char) [lines 1182-1202] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-47-fixed/src/java/org/apache/commons/lang/text/StrBuilder.java =====
    public StrBuilder appendFixedWidthPadLeft(Object obj, int width, char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = (obj == null ? getNullText() : obj.toString());
            if (str == null) {
                str = "";
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

// ===== FIXED org.apache.commons.lang.text.StrBuilder :: appendFixedWidthPadRight(Object, int, char) [lines 1229-1249] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-47-fixed/src/java/org/apache/commons/lang/text/StrBuilder.java =====
    public StrBuilder appendFixedWidthPadRight(Object obj, int width, char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = (obj == null ? getNullText() : obj.toString());
            if (str == null) {
                str = "";
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
