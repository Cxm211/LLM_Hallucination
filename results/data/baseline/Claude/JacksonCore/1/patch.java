public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
{
    if (numStr == null || numStr.isEmpty()) {
        throw new NumberFormatException("empty String");
    }
    return new BigDecimal(numStr);
}