public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i] & 0xFF;
            if (b == ESCAPE_CHAR) {
                // Check for soft line break: "=\r\n" or "=\n"
                if (i + 2 < bytes.length && bytes[i + 1] == '\r' && bytes[i + 2] == '\n') {
                    i += 2; // skip CRLF
                    continue;
                } else if (i + 1 < bytes.length && bytes[i + 1] == '\n') {
                    i += 1; // skip LF only
                    continue;
                } else {
                    try {
                        int u = Utils.digit16(bytes[++i] & 0xFF);
                        int l = Utils.digit16(bytes[++i] & 0xFF);
                        buffer.write((char) ((u << 4) + l));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new DecoderException("Invalid quoted-printable encoding", e);
                    }
                }
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }