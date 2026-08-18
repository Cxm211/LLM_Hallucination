public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
            throws NumberFormatException
    {
            if (len == 3) {
                if (buffer[offset] == 'N' && buffer[offset+1] == 'a' && buffer[offset+2] == 'N') {
                    throw new NumberFormatException("Non-standard token 'NaN': enable Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                }
            } else if (len == 8) {
                if (buffer[offset] == 'I' && buffer[offset+1] == 'n' && buffer[offset+2] == 'f'
                        && buffer[offset+3] == 'i' && buffer[offset+4] == 'n' && buffer[offset+5] == 'i'
                        && buffer[offset+6] == 't' && buffer[offset+7] == 'y') {
                    throw new NumberFormatException("Non-standard token 'Infinity': enable Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                }
            } else if (len == 9) {
                char c = buffer[offset];
                if ((c == '-' || c == '+')
                        && buffer[offset+1] == 'I' && buffer[offset+2] == 'n' && buffer[offset+3] == 'f'
                        && buffer[offset+4] == 'i' && buffer[offset+5] == 'n' && buffer[offset+6] == 'i'
                        && buffer[offset+7] == 't' && buffer[offset+8] == 'y') {
                    String token = (c == '-') ? "-Infinity" : "+Infinity";
                    throw new NumberFormatException("Non-standard token '" + token + "': enable Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                }
            }
            return new BigDecimal(buffer, offset, len);
    }