public double doubleValue() {
    double result = numerator.doubleValue() / denominator.doubleValue();
    if (Double.isInfinite(result) || Double.isNaN(result)) {
        int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - Math.getExponent(Double.MAX_VALUE);
        if (shift > 0) {
            result = numerator.shiftRight(shift).doubleValue() / denominator.shiftRight(shift).doubleValue();
        }
    }
    return result;
}