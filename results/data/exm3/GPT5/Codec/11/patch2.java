public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            // Skip raw CR and LF (not encoded and not part of soft line break)
            if (b == '\r' || b == '\n') {
                continue;
            }
            if (b == ESCAPE_CHAR) {
                // handle soft line breaks and hex encodings
                if (i + 1 >= bytes.length) {
                    throw new DecoderException("Invalid quoted-printable encoding");
                }
                int next = bytes[i + 1] & 0xFF;
                // soft line break "=\r\n" or tolerant "=\n" or "=\r"
                if (next == '\r') {
                    if (i + 2 < bytes.length && (bytes[i + 2] & 0xFF) == '\n') {
                        i += 2; // skip =\r\n
                    } else {
                        i += 1; // skip =\r (tolerant)
                    }
                    continue;
                } else if (next == '\n') {
                    i += 1; // skip =\n (tolerant)
                    continue;
                } else {
                    // must be two hex digits
                    if (i + 2 >= bytes.length) {
                        throw new DecoderException("Invalid quoted-printable encoding");
                    }
                    try {
                        int u = Utils.digit16(bytes[++i]);
                        int l = Utils.digit16(bytes[++i]);
                        buffer.write((u << 4) + l);
                    } catch (RuntimeException e) {
                        throw new DecoderException("Invalid quoted-printable encoding", e);
                    }
                }
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }