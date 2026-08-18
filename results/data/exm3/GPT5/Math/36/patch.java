public double doubleValue() {
        // Use BigDecimal with a reasonable MathContext to avoid Infinity/NaN when
        // numerator and denominator are too large for direct double conversion.
        try {
            return new java.math.BigDecimal(numerator)
                .divide(new java.math.BigDecimal(denominator), java.math.MathContext.DECIMAL64)
                .doubleValue();
        } catch (ArithmeticException ex) {
            // Fallback to direct conversion if anything goes wrong (e.g., unexpected issues),
            // preserving original behavior as a last resort.
            return numerator.doubleValue() / denominator.doubleValue();
        }
    }