public float floatValue() {
        if (denominator.signum() == 0) {
            int ns = numerator.signum();
            if (ns > 0) return Float.POSITIVE_INFINITY;
            if (ns < 0) return Float.NEGATIVE_INFINITY;
            return Float.NaN;
        }
        java.math.BigDecimal num = new java.math.BigDecimal(numerator);
        java.math.BigDecimal den = new java.math.BigDecimal(denominator);
        return num.divide(den, java.math.MathContext.DECIMAL32).floatValue();
    }