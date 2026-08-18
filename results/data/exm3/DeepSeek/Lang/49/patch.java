    public Fraction reduce() {
        if (numerator == 0) {
            return Fraction.ZERO;
        }
        long a = numerator;
        long b = denominator;
        long absA = a < 0 ? -a : a;
        long absB = b < 0 ? -b : b;
        long gcd = absA;
        long temp = absB;
        while (temp != 0) {
            long remainder = gcd % temp;
            gcd = temp;
            temp = remainder;
        }
        long newNum = a / gcd;
        long newDen = b / gcd;
        if (newDen < 0) {
            newNum = -newNum;
            newDen = -newDen;
        }
        int intNum = (int) newNum;
        int intDen = (int) newDen;
        if (intNum == numerator && intDen == denominator) {
            return this;
        }
        return Fraction.getFraction(intNum, intDen);
    }