public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
        throws NumberFormatException
{
    if (buffer == null || len == 0) {
        throw new NumberFormatException("empty String");
    }
    return new BigDecimal(buffer, offset, len);
}