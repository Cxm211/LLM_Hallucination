public float floatValue() {
    float result = numerator.floatValue() / denominator.floatValue();
    if (Float.isInfinite(result) || Float.isNaN(result)) {
        int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - Math.getExponent(Float.MAX_VALUE);
        if (shift > 0) {
            result = numerator.shiftRight(shift).floatValue() / denominator.shiftRight(shift).floatValue();
        }
    }
    return result;
}