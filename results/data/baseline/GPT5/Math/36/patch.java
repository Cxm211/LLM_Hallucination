public double doubleValue() {
        if (denominator.signum() == 0) {
            int ns = numerator.signum();
            if (ns > 0) return Double.POSITIVE_INFINITY;
            if (ns < 0) return Double.NEGATIVE_INFINITY;
            return Double.NaN;
        }
        java.math.BigDecimal num = new java.math.BigDecimal(numerator);
        java.math.BigDecimal den = new java.math.BigDecimal(denominator);
        return num.divide(den, java.math.MathContext.DECIMAL64).doubleValue();
    }