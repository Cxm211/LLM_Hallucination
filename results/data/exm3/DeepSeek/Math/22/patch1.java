    public boolean isSupportUpperBoundInclusive() {
        final double upper = getSupportUpperBound();
        final double dens = density(upper);
        return !Double.isInfinite(upper) && !Double.isNaN(dens) && !Double.isInfinite(dens);
    }