public void writeRaw(String text, int offset, int len) throws IOException
{
    final char[] buf = _charBuffer;

    // minor optimization: see if we can just get and copy

    // If not, need segmented approach. For speed, let's also use input buffer
    // size that is guaranteed to fit in output buffer; each char can expand to
    // at most 3 bytes, so at most 1/3 of buffer size.

    while (len > 0) {
        int len2 = Math.min(buf.length, len);
        text.getChars(offset, offset+len2, buf, 0);
        // If this is NOT the last segment and if the last character looks like
        // split surrogate second half, drop it
        if (len > len2 && len2 > 0) {
            char lastChar = buf[len2 - 1];
            if (lastChar >= SURR1_FIRST && lastChar <= SURR1_LAST) {
                len2--;
            }
        }
        _writeSegmentedRaw(buf, 0, len2);
        offset += len2;
        len -= len2;
    }
}