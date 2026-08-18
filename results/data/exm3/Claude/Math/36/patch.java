public double doubleValue() {
    double result = numerator.doubleValue() / denominator.doubleValue();
    if (Double.isInfinite(result) || result == 0.0) {
        // Numerator and/or denominator must be out of range:
        // Calculate how far to shift them to put them in range.
        int numBits = numerator.bitLength();
        int denBits = denominator.bitLength();
        int shift = Math.max(numBits, denBits) - 1023;
        if (shift > 0) {
            result = numerator.shiftRight(shift).doubleValue() / denominator.shiftRight(shift).doubleValue();
        }
    }
    return result;
}