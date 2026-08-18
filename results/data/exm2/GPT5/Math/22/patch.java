public boolean isSupportLowerBoundInclusive() {
    final double lowerBound = getSupportLowerBound();
    final double result = density(lowerBound);
    return !Double.isInfinite(lowerBound) && !Double.isNaN(result) && !Double.isInfinite(result);
}