public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
        throws NumberFormatException
{
    if (len == 0) {
        throw new NumberFormatException("empty String");
    }
    // Trim leading and trailing whitespace
    int start = offset;
    int end = offset + len;
    while (start < end && Character.isWhitespace(buffer[start])) {
        start++;
    }
    while (end > start && Character.isWhitespace(buffer[end - 1])) {
        end--;
    }
    int newLen = end - start;
    if (newLen == 0) {
        throw new NumberFormatException("empty String");
    }
    return new BigDecimal(buffer, start, newLen);
}