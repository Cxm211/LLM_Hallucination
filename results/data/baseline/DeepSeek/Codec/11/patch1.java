public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int MAX_LINE_LENGTH = 76;
        int column = 0;
        for (byte c : bytes) {
            int b = c & 0xFF;
            // Rule #3: whitespace at end of line must be encoded, so always encode space and tab
            if (b == ' ' || b == '\t') {
                if (column + 3 > MAX_LINE_LENGTH) {
                    buffer.write('\r');
                    buffer.write('\n');
                    column = 0;
                }
                encodeQuotedPrintable(b, buffer);
                column += 3;
            } else if (printable.get(b)) {
                if (column + 1 > MAX_LINE_LENGTH) {
                    buffer.write('\r');
                    buffer.write('\n');
                    column = 0;
                }
                buffer.write(b);
                column++;
            } else {
                if (column + 3 > MAX_LINE_LENGTH) {
                    buffer.write('\r');
                    buffer.write('\n');
                    column = 0;
                }
                encodeQuotedPrintable(b, buffer);
                column += 3;
            }
        }
        return buffer.toByteArray();
    }