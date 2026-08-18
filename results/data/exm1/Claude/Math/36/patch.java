public double doubleValue() {
    double result = numerator.doubleValue() / denominator.doubleValue();
    if (Double.isNaN(result)) {
        int shift = Math.max(numerator.bitLength(), denominator.bitLength()) - 1023;
        result = numerator.shiftRight(shift).doubleValue() / denominator.shiftRight(shift).doubleValue();
    }
    return result;
}