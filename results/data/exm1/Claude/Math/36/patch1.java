public float floatValue() {
    float result = numerator.floatValue() / denominator.floatValue();
    if (Float.isNaN(result)) {
        int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - 127;
        result = numerator.shiftRight(shift).floatValue() / denominator.shiftRight(shift).floatValue();
    }
    return result;
}