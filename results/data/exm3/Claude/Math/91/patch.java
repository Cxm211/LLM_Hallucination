public int compareTo(Fraction object) {
    if (this.numerator * object.denominator < object.numerator * this.denominator) {
        return -1;
    } else if (this.numerator * object.denominator > object.numerator * this.denominator) {
        return 1;
    } else {
        return 0;
    }
}