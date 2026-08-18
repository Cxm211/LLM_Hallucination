public int compareTo(Fraction object) {
    if (this == object) {
        return 0;
    }
    long nOd = ((long) numerator) * object.denominator;
    long dOn = ((long) denominator) * object.numerator;
    if (nOd < dOn) {
        return -1;
    } else if (nOd > dOn) {
        return 1;
    } else {
        return 0;
    }
}