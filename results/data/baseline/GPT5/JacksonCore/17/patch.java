public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            // Avoid splitting a surrogate pair across segments
            if (len2 < len) {
                char last = text.charAt(offset + len2 - 1);
                if (Character.isHighSurrogate(last) && len2 > 1) {
                    --len2;
                }
            }
            if (len2 <= 0) {
                // Fallback; should not generally happen with reasonable buffer sizes
                len2 = Math.min(buf.length, len);
            }
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }