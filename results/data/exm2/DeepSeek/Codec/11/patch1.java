    public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int pos = 0;
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            boolean encode = false;
            int encodedLen = 1;
            if (!printable.get(b)) {
                encode = true;
                encodedLen = 3;
            } else if (b == ' ' || b == '\t') {
                if (pos == 75) {
                    encode = true;
                    encodedLen = 3;
                } else {
                    encode = false;
                    encodedLen = 1;
                }
            } else {
                encode = false;
                encodedLen = 1;
            }
            if (bytes.length - i < 6 && pos > 0) {
                buffer.write(ESCAPE_CHAR);
                buffer.write('\r');
                buffer.write('\n');
                pos = 0;
            }
            if (pos + encodedLen > 75) {
                buffer.write(ESCAPE_CHAR);
                buffer.write('\r');
                buffer.write('\n');
                pos = 0;
            }
            if (encode) {
                encodeQuotedPrintable(b, buffer);
            } else {
                buffer.write(b);
            }
            pos += encodedLen;
        }
        return buffer.toByteArray();
    }