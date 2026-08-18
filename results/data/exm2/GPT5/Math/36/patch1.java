public float floatValue() {
    java.math.BigDecimal num = new java.math.BigDecimal(numerator);
    java.math.BigDecimal den = new java.math.BigDecimal(denominator);
    java.math.BigDecimal bd = num.divide(den, java.math.MathContext.DECIMAL32);
    return bd.floatValue();
}