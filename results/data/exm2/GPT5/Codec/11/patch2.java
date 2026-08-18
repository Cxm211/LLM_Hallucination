public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    if (i + 1 < bytes.length) {
                        int c = bytes[i + 1];
                        if (c == '\r') {
                            if (i + 2 < bytes.length && bytes[i + 2] == '\n') {
                                i += 2;
                                continue;
                            } else {
                                throw new DecoderException("Invalid quoted-printable encoding");
                            }
                        } else if (c == '\n') {
                            i += 1;
                            continue;
                        } else {
                            int u = Utils.digit16(bytes[++i]);
                            int l = Utils.digit16(bytes[++i]);
                            buffer.write((char) ((u << 4) + l));
                        }
                    } else {
                        throw new DecoderException("Invalid quoted-printable encoding");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid quoted-printable encoding", e);
                }
            } else if (b == '\r' || b == '\n') {
                // skip raw CR/LF
                continue;
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }