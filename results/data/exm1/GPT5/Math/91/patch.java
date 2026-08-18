public int compareTo(Fraction object) {
        long lhs = (long) getNumerator() * (long) object.getDenominator();
        long rhs = (long) object.getNumerator() * (long) getDenominator();
        return (lhs < rhs) ? -1 : ((lhs > rhs) ? +1 : 0);
    }