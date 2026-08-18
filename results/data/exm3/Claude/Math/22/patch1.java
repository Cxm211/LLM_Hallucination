public boolean isSupportUpperBoundInclusive() {
    final double upperBound = getSupportUpperBound();
    if (Double.isInfinite(upperBound)) {
        return false;
    }
    final double result = density(upperBound);
    return !Double.isNaN(result) && !Double.isInfinite(result);
}