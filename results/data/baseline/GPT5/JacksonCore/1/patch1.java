public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
            throws NumberFormatException
    {
            if (buffer == null) {
                throw new NumberFormatException("null");
            }
            if (offset < 0 || len < 0 || offset + len > buffer.length) {
                throw new NumberFormatException("Invalid range");
            }
            return new BigDecimal(buffer, offset, len);
    }