    public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
    {
        if ("NaN".equals(numStr) || "Infinity".equals(numStr) || "-Infinity".equals(numStr)) {
            return null;
        }
        return new BigDecimal(numStr);
    }