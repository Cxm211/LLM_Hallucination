    public float floatValue() {
        float result = numerator.floatValue() / denominator.floatValue();
        if (Float.isInfinite(result)) {
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
            int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - 127;
            if (shift > 0) {
                BigInteger shiftedNumerator = numerator.shiftRight(shift);
                BigInteger shiftedDenominator = denominator.shiftRight(shift);
                result = shiftedNumerator.floatValue() / shiftedDenominator.floatValue();
            }
        } else if (result == 0.0f && numerator.signum() != 0) {
            int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - 127;
            if (shift > 0) {
                BigInteger shiftedNumerator = numerator.shiftRight(shift);
                BigInteger shiftedDenominator = denominator.shiftRight(shift);
                result = shiftedNumerator.floatValue() / shiftedDenominator.floatValue();
            }
        }
        return result;
    }