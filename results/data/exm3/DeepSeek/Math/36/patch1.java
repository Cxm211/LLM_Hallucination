    public float floatValue() {
        float result = numerator.floatValue() / denominator.floatValue();
        if (Float.isNaN(result)) {
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
            int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - Float.MAX_EXPONENT;
            if (shift > 0) {
                BigInteger scaledNumerator = numerator.shiftRight(shift);
                BigInteger scaledDenominator = denominator.shiftRight(shift);
                result = scaledNumerator.floatValue() / scaledDenominator.floatValue();
            }
        }
        return result;
    }