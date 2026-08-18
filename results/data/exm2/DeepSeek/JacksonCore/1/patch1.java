    public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
            throws NumberFormatException
    {
        if (len == 3 && buffer[offset] == 'N' && buffer[offset+1] == 'a' && buffer[offset+2] == 'N') {
            return null;
        }
        if (len == 8 && buffer[offset] == 'I' && buffer[offset+1] == 'n' && buffer[offset+2] == 'f'
                && buffer[offset+3] == 'i' && buffer[offset+4] == 'n' && buffer[offset+5] == 'i'
                && buffer[offset+6] == 't' && buffer[offset+7] == 'y') {
            return null;
        }
        if (len == 9 && buffer[offset] == '-' && buffer[offset+1] == 'I' && buffer[offset+2] == 'n'
                && buffer[offset+3] == 'f' && buffer[offset+4] == 'i' && buffer[offset+5] == 'n'
                && buffer[offset+6] == 'i' && buffer[offset+7] == 't' && buffer[offset+8] == 'y') {
            return null;
        }
        return new BigDecimal(buffer, offset, len);
    }