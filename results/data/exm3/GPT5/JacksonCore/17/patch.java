public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            // avoid splitting surrogate pair between segments
            if (len2 < len) {
                int last = offset + len2 - 1;
                char ch = text.charAt(last);
                if (ch >= SURR1_FIRST && ch <= SURR1_LAST) {
                    --len2;
                    if (len2 == 0) {
                        // if buffer would end with leading surrogate and nothing else fits,
                        // defer to next round to include the pair together
                        continue;
                    }
                }
            }
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }