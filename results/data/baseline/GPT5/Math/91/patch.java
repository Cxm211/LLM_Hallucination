public int compareTo(Fraction object) {
        if (object == null) {
            throw new NullPointerException("Fraction is null");
        }
        if (this == object) {
            return 0;
        }
        long lhs = (long) this.getNumerator() * object.getDenominator();
        long rhs = (long) object.getNumerator() * this.getDenominator();
        return (lhs < rhs) ? -1 : ((lhs > rhs) ? 1 : 0);
    }