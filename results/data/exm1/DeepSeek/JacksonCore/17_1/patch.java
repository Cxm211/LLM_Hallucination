    public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            text.getChars(offset, offset+len2, buf, 0);
            // Avoid splitting surrogate pair: if last char is high surrogate and there is more data, drop it
            if (len2 > 0 && len > len2) {
                char last = buf[len2-1];
                if (last >= 0xD800 && last <= 0xDBFF) {
                    len2--;
                }
            }
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }