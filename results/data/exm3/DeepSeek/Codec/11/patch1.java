    public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int MAX_LINE_LENGTH = 76;
        int lineLength = 0;
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            boolean isPrintable = printable.get(b);
            boolean isWhitespace = (b == 32 || b == 9); // space or tab
            boolean needEncode = false;
            int encodedLen = 1;
            
            if (!isPrintable) {
                needEncode = true;
                encodedLen = 3;
            } else if (isWhitespace) {
                if (i == bytes.length - 1) {
                    needEncode = true;
                } else {
                    int nextB = bytes[i+1] & 0xFF;
                    boolean nextPrintable = printable.get(nextB);
                    int nextLen = 1;
                    if (!nextPrintable) {
                        nextLen = 3;
                    } else if (nextB == 32 || nextB == 9) {
                        nextLen = 1;
                    }
                    if (lineLength + 1 + nextLen > MAX_LINE_LENGTH) {
                        needEncode = true;
                    }
                }
                if (needEncode) {
                    encodedLen = 3;
                }
            } else {
                needEncode = false;
                encodedLen = 1;
            }
            
            if (lineLength + encodedLen > MAX_LINE_LENGTH) {
                buffer.write(ESCAPE_CHAR);
                buffer.write('\r');
                buffer.write('\n');
                lineLength = 0;
            }
            
            if (needEncode) {
                encodeQuotedPrintable(b, buffer);
            } else {
                buffer.write(b);
            }
            lineLength += encodedLen;
        }
        return buffer.toByteArray();
    }