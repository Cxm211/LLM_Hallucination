public Fraction reduce() {
    int num = numerator;
    int den = denominator;
    if (den < 0) {
        num = -num;
        den = -den;
    }
    int gcd = greatestCommonDivisor(Math.abs(num), den);
    if (gcd == 1) {
        if (numerator == num && denominator == den) {
            return this;
        }
        return Fraction.getFraction(num, den);
    }
    return Fraction.getFraction(num / gcd, den / gcd);
}