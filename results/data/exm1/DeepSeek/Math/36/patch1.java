public float floatValue() {
    BigInteger tmpNum = numerator;
    BigInteger tmpDen = denominator;
    int numBits = tmpNum.bitLength();
    int denBits = tmpDen.bitLength();
    if (numBits > 24 || denBits > 24) {
        int shift = Math.min(numBits, denBits) - 24;
        if (shift > 0) {
            tmpNum = tmpNum.shiftRight(shift);
            tmpDen = tmpDen.shiftRight(shift);
        }
    }
    float result = tmpNum.floatValue() / tmpDen.floatValue();
    return result;
}