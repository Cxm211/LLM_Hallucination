public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
{
    if (numStr == null) {
        throw new NumberFormatException("null string");
    }
    numStr = numStr.trim();
    if (numStr.isEmpty()) {
        throw new NumberFormatException("empty string");
    }
    return new BigDecimal(numStr);
}