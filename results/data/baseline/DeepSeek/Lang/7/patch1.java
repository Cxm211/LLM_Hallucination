public static BigDecimal createBigDecimal(String str) {
    if (str == null) {
        return null;
    }
    if (StringUtils.isBlank(str)) {
        throw new NumberFormatException("A blank string is not a valid number");
    }
    return new BigDecimal(str);
}