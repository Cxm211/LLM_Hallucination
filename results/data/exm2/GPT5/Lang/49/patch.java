public Fraction reduce() {
    if (numerator == 0) {
        if (denominator == 1) {
            return this;
        }
        return Fraction.ZERO;
    }
    int gcd = greatestCommonDivisor(Math.abs(numerator), Math.abs(denominator));
    int num = numerator / gcd;
    int den = denominator / gcd;
    if (den < 0) {
        num = -num;
        den = -den;
    }
    if (gcd == 1 && den == denominator) {
        return this;
    }
    return Fraction.getFraction(num, den);
}