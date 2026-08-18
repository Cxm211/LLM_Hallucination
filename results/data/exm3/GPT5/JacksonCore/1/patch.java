public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
    {
            int len = numStr.length();
            if (len == 3) {
                if ("NaN".equals(numStr)) {
                    throw new NumberFormatException("Non-standard token 'NaN': enable Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                }
            } else if (len == 8) {
                if ("Infinity".equals(numStr)) {
                    throw new NumberFormatException("Non-standard token 'Infinity': enable Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                }
            } else if (len == 9) {
                if ("-Infinity".equals(numStr) || "+Infinity".equals(numStr)) {
                    throw new NumberFormatException("Non-standard token '" + numStr + "': enable Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                }
            }
            return new BigDecimal(numStr);
    }