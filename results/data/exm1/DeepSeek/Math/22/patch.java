public boolean isSupportLowerBoundInclusive() {
    double lower = getSupportLowerBound();
    if (Double.isInfinite(lower)) return false;
    double density = density(lower);
    return !Double.isNaN(density) && !Double.isInfinite(density);
}