    public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            // If not the last segment, check if the last character is a high surrogate
            if (len2 < len && len2 > 0) {
                char c = text.charAt(offset + len2 - 1);
                if (c >= '\uD800' && c <= '\uDBFF') {
                    // high surrogate, avoid splitting by reducing segment length
                    len2--;
                }
            }
            if (len2 == 0) {
                // This can happen if buf.length == 1 and the first char is a high surrogate.
                // In that case, we cannot avoid splitting; write the single char anyway.
                len2 = 1;
            }
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }