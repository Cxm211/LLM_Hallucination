public void writeRaw(String text, int offset, int len) throws IOException
{
    final char[] buf = _charBuffer;

    while (len > 0) {
        int len2 = Math.min(buf.length, len);
        text.getChars(offset, offset+len2, buf, 0);
        
        if (len2 > len) {
            if (len2 > 0 && Character.isHighSurrogate(buf[len2-1])) {
                --len2;
            }
        }
        writeRaw(buf, 0, len2);
        offset += len2;
        len -= len2;
    }
}