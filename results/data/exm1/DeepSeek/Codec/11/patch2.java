public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
    if (bytes == null) {
        return null;
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    for (int i = 0; i < bytes.length; i++) {
        final int b = bytes[i] & 0xFF;
        if (b == ESCAPE_CHAR) {
            // Check for soft line break
            if (i + 2 < bytes.length && bytes[i+1] == '\r' && bytes[i+2] == '\n') {
                i += 2;
                continue;
            }
            try {
                int u = Utils.digit16(bytes[++i]);
                int l = Utils.digit16(bytes[++i]);
                if (u == -1 || l == -1) {
                    throw new DecoderException("Invalid quoted-printable encoding");
                }
                buffer.write((char) ((u << 4) + l));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new DecoderException("Invalid quoted-printable encoding", e);
            }
        } else {
            buffer.write(b);
        }
    }
    return buffer.toByteArray();
}