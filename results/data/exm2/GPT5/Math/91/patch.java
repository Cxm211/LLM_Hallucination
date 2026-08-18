public int compareTo(Fraction object) {
    if (this == object) {
        return 0;
    }
    long left = (long) this.getNumerator() * (long) object.getDenominator();
    long right = (long) object.getNumerator() * (long) this.getDenominator();
    return left < right ? -1 : (left > right ? 1 : 0);
}