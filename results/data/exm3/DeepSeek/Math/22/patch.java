    public boolean isSupportLowerBoundInclusive() {
        final double lower = getSupportLowerBound();
        final double dens = density(lower);
        return !Double.isInfinite(lower) && !Double.isNaN(dens) && !Double.isInfinite(dens);
    }