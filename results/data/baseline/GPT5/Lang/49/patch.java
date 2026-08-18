public Fraction reduce() {
        if (numerator == 0) {
            return Fraction.getFraction(0, 1);
        }
        int gcd = greatestCommonDivisor(Math.abs(numerator), Math.abs(denominator));
        int num = numerator / gcd;
        int den = denominator / gcd;
        if (den < 0) {
            num = -num;
            den = -den;
        }
        if (num == numerator && den == denominator) {
            return this;
        }
        return Fraction.getFraction(num, den);
    }