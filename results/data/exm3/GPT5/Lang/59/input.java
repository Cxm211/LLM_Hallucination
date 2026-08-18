// buggy function
    public StrBuilder appendFixedWidthPadRight(Object obj, int width, char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = (obj == null ? getNullText() : obj.toString());
            int strLen = str.length();
            if (strLen >= width) {
                str.getChars(0, strLen, buffer, size);
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

// trigger testcase
// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java::testLang299
public void testLang299() {
        StrBuilder sb = new StrBuilder(1);
        sb.appendFixedWidthPadRight("foo", 1, '-');
        assertEquals("f", sb.toString());
    }
