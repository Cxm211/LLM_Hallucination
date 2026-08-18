public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
    if (p < 0) {
        throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
    }
    mean = p;
    if (p == 0) {
        normal = null;
    } else {
        normal = new NormalDistributionImpl(p, FastMath.sqrt(p));
    }
    this.epsilon = epsilon;
    this.maxIterations = maxIterations;
}