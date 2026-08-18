    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
        double n = getNumeratorDegreesOfFreedom();
        double q = Math.max(Math.min(p, 1.0 - 1e-10), 1e-10);
        ret = (n / d) * (q / (1.0 - q));
        return ret;
    }