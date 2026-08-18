public double doubleValue() {
    BigInteger tmpNum = numerator;
    BigInteger tmpDen = denominator;
    int numBits = tmpNum.bitLength();
    int denBits = tmpDen.bitLength();
    if (numBits > 53 || denBits > 53) {
        int shift = Math.min(numBits, denBits) - 53;
        if (shift > 0) {
            tmpNum = tmpNum.shiftRight(shift);
            tmpDen = tmpDen.shiftRight(shift);
        }
    }
    double result = tmpNum.doubleValue() / tmpDen.doubleValue();
    return result;
}