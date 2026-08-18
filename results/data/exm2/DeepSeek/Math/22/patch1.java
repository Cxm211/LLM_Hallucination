    public boolean isSupportUpperBoundInclusive() {
        final double upperBound = getSupportUpperBound();
        final double result = density(upperBound);
        return !Double.isInfinite(upperBound) && !Double.isNaN(result) && !Double.isInfinite(result);
    }