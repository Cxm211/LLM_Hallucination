public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        final int MAX_LINE_LENGTH = 76;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int pos = 0; // current line position
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            boolean isPrintable = printable.get(b) && b != '=';
            if (isPrintable) {
                boolean isWhitespace = (b == ' ' || b == '\t');
                if (isWhitespace) {
                    // whitespace at end of line must be encoded
                    if (pos == MAX_LINE_LENGTH - 1) {
                        // need to encode; ensure room
                        if (pos + 3 > MAX_LINE_LENGTH) {
                            buffer.write('=');
                            buffer.write('\r');
                            buffer.write('\n');
                            pos = 0;
                        }
                        encodeQuotedPrintable(b, buffer);
                        pos += 3;
                    } else {
                        // safe to write literally
                        if (pos + 1 > MAX_LINE_LENGTH) {
                            buffer.write('=');
                            buffer.write('\r');
                            buffer.write('\n');
                            pos = 0;
                        }
                        buffer.write(b);
                        pos += 1;
                    }
                } else {
                    if (pos + 1 > MAX_LINE_LENGTH) {
                        buffer.write('=');
                        buffer.write('\r');
                        buffer.write('\n');
                        pos = 0;
                    }
                    buffer.write(b);
                    pos += 1;
                }
            } else {
                // needs encoding (including '=')
                if (pos + 3 > MAX_LINE_LENGTH) {
                    buffer.write('=');
                    buffer.write('\r');
                    buffer.write('\n');
                    pos = 0;
                }
                encodeQuotedPrintable(b, buffer);
                pos += 3;
            }
        }
        return buffer.toByteArray();
    }