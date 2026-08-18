public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
    {
            if (numStr == null) {
                throw new NumberFormatException("null");
            }
            return new BigDecimal(numStr);
    }