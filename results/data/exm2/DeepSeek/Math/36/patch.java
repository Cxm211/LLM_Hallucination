public double doubleValue() {
    double result = numerator.doubleValue() / denominator.doubleValue();
    if (Double.isNaN(result)) {
        int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - Double.MAX_EXPONENT - 1;
        if (shift > 0) {
            BigInteger n = numerator.shiftRight(shift);
            BigInteger d = denominator.shiftRight(shift);
            result = n.doubleValue() / d.doubleValue();
        }
    }
    return result;
}