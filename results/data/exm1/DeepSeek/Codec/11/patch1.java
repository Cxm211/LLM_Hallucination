public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
    if (bytes == null) {
        return null;
    }
    if (printable == null) {
        printable = PRINTABLE_CHARS;
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int lineLength = 0;
    final int MAX_LINE_LENGTH = 76;
    boolean lastWasWhitespace = false;
    StringBuilder pending = new StringBuilder();
    for (byte c : bytes) {
        int b = c & 0xFF;
        if (b == ' ' || b == '\t') {
            pending.append((char)b);
            lastWasWhitespace = true;
            continue;
        }
        if (lastWasWhitespace) {
            // flush pending whitespace
            for (int i = 0; i < pending.length(); i++) {
                char ch = pending.charAt(i);
                if (lineLength + 1 > MAX_LINE_LENGTH) {
                    buffer.write('=');
                    buffer.write('\r');
                    buffer.write('\n');
                    lineLength = 0;
                }
                buffer.write(ch);
                lineLength++;
            }
            pending.setLength(0);
            lastWasWhitespace = false;
        }
        String encoded;
        if (printable.get(b) && b != '=') {
            encoded = String.valueOf((char)b);
        } else {
            encoded = "=" + String.format("%02X", b);
        }
        if (lineLength + encoded.length() > MAX_LINE_LENGTH) {
            buffer.write('=');
            buffer.write('\r');
            buffer.write('\n');
            lineLength = 0;
        }
        for (char ch : encoded.toCharArray()) {
            buffer.write(ch);
        }
        lineLength += encoded.length();
    }
    // flush any remaining whitespace at end
    if (pending.length() > 0) {
        for (int i = 0; i < pending.length(); i++) {
            char ch = pending.charAt(i);
            String encoded;
            if (ch == ' ' || ch == '\t') {
                encoded = "=" + String.format("%02X", (int)ch);
            } else {
                encoded = String.valueOf(ch);
            }
            if (lineLength + encoded.length() > MAX_LINE_LENGTH) {
                buffer.write('=');
                buffer.write('\r');
                buffer.write('\n');
                lineLength = 0;
            }
            for (char e : encoded.toCharArray()) {
                buffer.write(e);
            }
            lineLength += encoded.length();
        }
        pending.setLength(0);
    }
    return buffer.toByteArray();
}