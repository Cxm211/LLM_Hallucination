    public int compareTo(Fraction object) {
        long lhs = (long) this.numerator * object.denominator;
        long rhs = (long) object.numerator * this.denominator;
        if (lhs < rhs) {
            return -1;
        } else if (lhs > rhs) {
            return 1;
        } else {
            return 0;
        }
    }