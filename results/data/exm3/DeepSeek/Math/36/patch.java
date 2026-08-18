    public double doubleValue() {
        double result = numerator.doubleValue() / denominator.doubleValue();
        if (Double.isNaN(result)) {
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
            int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - Double.MAX_EXPONENT;
            if (shift > 0) {
                BigInteger scaledNumerator = numerator.shiftRight(shift);
                BigInteger scaledDenominator = denominator.shiftRight(shift);
                result = scaledNumerator.doubleValue() / scaledDenominator.doubleValue();
            }
        }
        return result;
    }