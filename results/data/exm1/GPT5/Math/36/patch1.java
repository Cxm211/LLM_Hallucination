public float floatValue() {
        java.math.BigDecimal num = new java.math.BigDecimal(numerator);
        java.math.BigDecimal den = new java.math.BigDecimal(denominator);
        return num.divide(den, java.math.MathContext.DECIMAL32).floatValue();
    }