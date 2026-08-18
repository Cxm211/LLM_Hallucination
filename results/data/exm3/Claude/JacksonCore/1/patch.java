public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
{
    String trimmed = numStr.trim();
    if (trimmed.isEmpty()) {
        throw new NumberFormatException("empty String");
    }
    return new BigDecimal(trimmed);
}