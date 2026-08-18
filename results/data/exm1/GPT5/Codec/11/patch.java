private static final void encodeQuotedPrintable(int b, ByteArrayOutputStream buffer) {
        buffer.write(ESCAPE_CHAR);
        char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
        char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
        buffer.write(hex1);
        buffer.write(hex2);
    }

    public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int pos = 0; // current line position (characters written in current line)
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b < 0) {
                b += 256;
            }

            boolean isSpaceOrTab = (b == ' ' || b == '\t');
            boolean isPrintable = printable.get(b) && b != ESCAPE_CHAR; // '=' must always be encoded

            // Determine if this space/tab must be encoded due to being at end-of-line
            boolean atEnd = (i + 1 >= bytes.length);
            int next = atEnd ? -1 : bytes[i + 1];
            if (!atEnd && next < 0) {
                next += 256;
            }
            boolean nextIsLineBreak = (next == '\r' || next == '\n');

            if (isPrintable && !isSpaceOrTab) {
                // plain printable char
                if (pos + 1 > 76) {
                    buffer.write('='); buffer.write('\r'); buffer.write('\n');
                    pos = 0;
                }
                buffer.write(b);
                pos += 1;
            } else if (isSpaceOrTab) {
                // whitespace: encode if at end of input or before a line break or would end the line
                boolean mustEncode = atEnd || nextIsLineBreak;
                if (!mustEncode && (pos + 1 > 76)) {
                    // would exceed line length, so break line before writing whitespace
                    buffer.write('='); buffer.write('\r'); buffer.write('\n');
                    pos = 0;
                }
                if (mustEncode) {
                    if (pos + 3 > 76) {
                        buffer.write('='); buffer.write('\r'); buffer.write('\n');
                        pos = 0;
                    }
                    encodeQuotedPrintable(b, buffer);
                    pos += 3;
                } else {
                    buffer.write(b);
                    pos += 1;
                }
            } else {
                // needs encoding (including '=' and non-printables, CR/LF always encoded here)
                if (pos + 3 > 76) {
                    buffer.write('='); buffer.write('\r'); buffer.write('\n');
                    pos = 0;
                }
                encodeQuotedPrintable(b, buffer);
                pos += 3;
            }
        }
        return buffer.toByteArray();
    }

    public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b == ESCAPE_CHAR) { // '='
                // soft line break handling
                if (i + 1 < bytes.length) {
                    int c1 = bytes[i + 1];
                    if (c1 == '\r' || c1 == '\n') {
                        // soft break: consume CR and optional LF (or lone LF)
                        i++;
                        if (c1 == '\r' && i + 1 < bytes.length && bytes[i + 1] == '\n') {
                            i++;
                        }
                        continue;
                    }
                }
                // hex decoding requires two hex digits
                if (i + 2 >= bytes.length) {
                    throw new DecoderException("Invalid quoted-printable encoding");
                }
                int u = Utils.digit16(bytes[i + 1]);
                int l = Utils.digit16(bytes[i + 2]);
                if (u == -1 || l == -1) {
                    throw new DecoderException("Invalid quoted-printable encoding");
                }
                buffer.write((u << 4) + l);
                i += 2;
            } else if (b == '\r' || b == '\n') {
                // skip raw CR/LF in the encoded stream
                continue;
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }