public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
        throws NumberFormatException
{
    if (buffer == null) {
        throw new NumberFormatException("null buffer");
    }
    if (len == 0) {
        throw new NumberFormatException("empty buffer");
    }
    return new BigDecimal(buffer, offset, len);
}