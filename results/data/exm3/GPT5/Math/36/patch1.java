public float floatValue() {
        // Use BigDecimal with a reasonable MathContext to avoid Infinity/NaN when
        // numerator and denominator are too large for direct float conversion.
        try {
            return new java.math.BigDecimal(numerator)
                .divide(new java.math.BigDecimal(denominator), java.math.MathContext.DECIMAL32)
                .floatValue();
        } catch (ArithmeticException ex) {
            // Fallback to direct conversion if anything goes wrong, preserving original behavior.
            return numerator.floatValue() / denominator.floatValue();
        }
    }