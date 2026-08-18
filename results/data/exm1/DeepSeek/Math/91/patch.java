    public int compareTo(Fraction object) {
        long nOd = (long)this.numerator * object.denominator;
        long dOn = (long)this.denominator * object.numerator;
        if (nOd < dOn) return -1;
        else if (nOd > dOn) return 1;
        else return 0;
    }