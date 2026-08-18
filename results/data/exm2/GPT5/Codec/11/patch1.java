public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int pos = 1;
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b < 0) {
                b = 256 + b;
            }
            // pass through CRLF sequences unchanged
            if (b == '\r') {
                buffer.write('\r');
                if (i + 1 < bytes.length && bytes[i + 1] == '\n') {
                    buffer.write('\n');
                    i++;
                }
                pos = 1;
                continue;
            } else if (b == '\n') {
                buffer.write('\n');
                pos = 1;
                continue;
            }
            if (printable.get(b) && b != ESCAPE_CHAR) {
                if (b == ' ' || b == '\t') {
                    int next = (i + 1 < bytes.length) ? bytes[i + 1] : -1;
                    if (next == '\r' || next == '\n' || next == -1) {
                        if (pos > 73) {
                            buffer.write(ESCAPE_CHAR);
                            buffer.write('\r');
                            buffer.write('\n');
                            pos = 1;
                        }
                        encodeQuotedPrintable(b, buffer);
                        pos += 3;
                    } else {
                        if (pos == 76) {
                            buffer.write(ESCAPE_CHAR);
                            buffer.write('\r');
                            buffer.write('\n');
                            pos = 1;
                        }
                        buffer.write(b);
                        pos++;
                    }
                } else {
                    if (pos == 76) {
                        buffer.write(ESCAPE_CHAR);
                        buffer.write('\r');
                        buffer.write('\n');
                        pos = 1;
                    }
                    buffer.write(b);
                    pos++;
                }
            } else {
                if (pos > 73) {
                    buffer.write(ESCAPE_CHAR);
                    buffer.write('\r');
                    buffer.write('\n');
                    pos = 1;
                }
                encodeQuotedPrintable(b, buffer);
                pos += 3;
            }
        }
        return buffer.toByteArray();
    }