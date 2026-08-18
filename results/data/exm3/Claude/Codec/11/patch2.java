public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
    if (bytes == null) {
        return null;
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    for (int i = 0; i < bytes.length; i++) {
        final int b = bytes[i];
        if (b == ESCAPE_CHAR) {
            try {
                // Check for soft line break (=\r\n or =\n)
                if (i + 1 < bytes.length) {
                    if (bytes[i + 1] == '\r') {
                        if (i + 2 < bytes.length && bytes[i + 2] == '\n') {
                            // Soft line break: =\r\n - skip all three
                            i += 2;
                            continue;
                        }
                    } else if (bytes[i + 1] == '\n') {
                        // Soft line break: =\n - skip both
                        i += 1;
                        continue;
                    }
                }
                
                // Otherwise decode hex digits
                int u = Utils.digit16(bytes[++i]);
                int l = Utils.digit16(bytes[++i]);
                buffer.write((char) ((u << 4) + l));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new DecoderException("Invalid quoted-printable encoding", e);
            }
        } else if (b == '\r' || b == '\n') {
            // Skip bare CR and LF
            continue;
        } else {
            // Append other octets
            buffer.write(b);
        }
    }
    return buffer.toByteArray();
}