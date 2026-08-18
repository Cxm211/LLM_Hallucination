    public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == ESCAPE_CHAR) {
                // handle soft line breaks (=\n or =\n)
                if (i + 1 < bytes.length) {
                    int next = bytes[i + 1];
                    if (next == '\r') {
                        i++; // skip CR
                        if (i + 1 < bytes.length && bytes[i + 1] == '\n') {
                            i++; // skip LF
                        }
                        continue;
                    } else if (next == '\n') {
                        i++; // skip LF
                        continue;
                    }
                }
                try {
                    int u = Utils.digit16(bytes[++i]);
                    int l = Utils.digit16(bytes[++i]);
                    buffer.write((u << 4) + l);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid quoted-printable encoding", e);
                }
            } else {
                // every other octet is appended except for CR & LF
                if (b != '\r' && b != '\n') {
                    buffer.write(b);
                }
            }
        }
        return buffer.toByteArray();
    }
