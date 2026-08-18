public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            // Avoid splitting surrogate pairs across segments: if the last char of this
            // segment is a high surrogate and there is more input to follow, back off by one
            if (len2 < len) {
                char last = text.charAt(offset + len2 - 1);
                if (last >= SURR1_FIRST && last <= SURR1_LAST) {
                    len2 -= 1;
                }
            }
            if (len2 == 0) { // safety, should not normally occur
                // if we get here, it means remaining input starts with a surrogate pair
                // too large for buffer; write single char directly to avoid stalling
                char c = text.charAt(offset);
                buf[0] = c;
                writeRaw(buf, 0, 1);
                offset += 1;
                len -= 1;
                continue;
            }
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }