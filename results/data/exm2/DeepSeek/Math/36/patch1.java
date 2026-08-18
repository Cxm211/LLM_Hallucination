public float floatValue() {
    float result = numerator.floatValue() / denominator.floatValue();
    if (Float.isNaN(result)) {
        int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - Float.MAX_EXPONENT - 1;
        if (shift > 0) {
            BigInteger n = numerator.shiftRight(shift);
            BigInteger d = denominator.shiftRight(shift);
            result = n.floatValue() / d.floatValue();
        }
    }
    return result;
}