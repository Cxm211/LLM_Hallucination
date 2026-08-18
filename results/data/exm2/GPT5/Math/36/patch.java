public double doubleValue() {
    java.math.BigDecimal num = new java.math.BigDecimal(numerator);
    java.math.BigDecimal den = new java.math.BigDecimal(denominator);
    java.math.BigDecimal bd = num.divide(den, java.math.MathContext.DECIMAL64);
    return bd.doubleValue();
}