public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
        if (Double.isNaN(p) || Double.isInfinite(p) || p <= 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
        }
        this.mean = p;
        this.normal = new NormalDistributionImpl(p, FastMath.sqrt(p));
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }