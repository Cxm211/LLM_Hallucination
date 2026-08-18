    public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        // minor optimization: see if we can just get and copy

        // If not, need segmented approach. For speed, let's also use input buffer
        // size that is guaranteed to fit in output buffer; each char can expand to
        // at most 3 bytes, so at most 1/3 of buffer size.

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            // check for possible split surrogate pair at chunk boundary
            if (len2 == buf.length && len2 > 0) {
                char lastChar = text.charAt(offset + len2 - 1);
                if (Character.isHighSurrogate(lastChar)) {
                    // avoid splitting surrogate pair, reduce chunk size by 1
                    len2--;
                }
            }
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            // If this is NOT the last segment and if the last character looks like
            // split surrogate second half, drop it
            offset += len2;
            len -= len2;
        }
    }