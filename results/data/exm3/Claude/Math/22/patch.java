public boolean isSupportLowerBoundInclusive() {
    final double lowerBound = getSupportLowerBound();
    if (Double.isInfinite(lowerBound)) {
        return false;
    }
    final double result = density(lowerBound);
    return !Double.isNaN(result) && !Double.isInfinite(result);
}