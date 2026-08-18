    public int compareTo(Fraction object) {
        long nOd = (long) numerator * object.denominator;
        long dOn = (long) object.numerator * denominator;
        return Long.compare(nOd, dOn);
    }