public Fraction reduce() {
    if (numerator == 0) {
        return Fraction.ZERO;
    }
    int gcd = greatestCommonDivisor(Math.abs(numerator), Math.abs(denominator));
    if (gcd == 1 && denominator > 0) {
        return this;
    }
    int newNum = numerator / gcd;
    int newDen = denominator / gcd;
    if (newDen < 0) {
        newNum = -newNum;
        newDen = -newDen;
    }
    return Fraction.getFraction(newNum, newDen);
}