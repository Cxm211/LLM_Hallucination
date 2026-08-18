    public double doubleValue() {
        double result = numerator.doubleValue() / denominator.doubleValue();
        if (Double.isInfinite(result)) {
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
            int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - 1023;
            if (shift > 0) {
                BigInteger shiftedNumerator = numerator.shiftRight(shift);
                BigInteger shiftedDenominator = denominator.shiftRight(shift);
                result = shiftedNumerator.doubleValue() / shiftedDenominator.doubleValue();
            }
        } else if (result == 0.0 && numerator.signum() != 0) {
            int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - 1023;
            if (shift > 0) {
                BigInteger shiftedNumerator = numerator.shiftRight(shift);
                BigInteger shiftedDenominator = denominator.shiftRight(shift);
                result = shiftedNumerator.doubleValue() / shiftedDenominator.doubleValue();
            }
        }
        return result;
    }