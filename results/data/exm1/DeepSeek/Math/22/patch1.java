public boolean isSupportUpperBoundInclusive() {
    double upper = getSupportUpperBound();
    if (Double.isInfinite(upper)) return false;
    double density = density(upper);
    return !Double.isNaN(density) && !Double.isInfinite(density);
}