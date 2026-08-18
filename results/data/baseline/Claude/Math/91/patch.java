public int compareTo(Fraction object) {
    if (object == null) {
        throw new NullPointerException();
    }
    long numer1 = (long) this.numerator * object.denominator;
    long numer2 = (long) object.numerator * this.denominator;
    if (numer1 < numer2) {
        return -1;
    } else if (numer1 > numer2) {
        return 1;
    } else {
        return 0;
    }
}