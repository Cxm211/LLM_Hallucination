public float floatValue() {
    float result = numerator.floatValue() / denominator.floatValue();
    if (Float.isInfinite(result) || result == 0.0f) {
        // Numerator and/or denominator must be out of range:
        // Calculate how far to shift them to put them in range.
        int numBits = numerator.bitLength();
        int denBits = denominator.bitLength();
        int shift = Math.max(numBits, denBits) - 127;
        if (shift > 0) {
            result = numerator.shiftRight(shift).floatValue() / denominator.shiftRight(shift).floatValue();
        }
    }
    return result;
}