    public int compareTo(Fraction object) {
        long left = (long) this.numerator * object.denominator;
        long right = (long) object.numerator * this.denominator;
        if (left < right) return -1;
        if (left > right) return 1;
        return 0;
    }