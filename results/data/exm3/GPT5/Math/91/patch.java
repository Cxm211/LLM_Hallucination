public int compareTo(Fraction object) {
        if (this == object) {
            return 0;
        }
        long lhs = (long) this.numerator * (long) object.denominator;
        long rhs = (long) object.numerator * (long) this.denominator;
        return (lhs < rhs) ? -1 : ((lhs > rhs) ? 1 : 0);
    }