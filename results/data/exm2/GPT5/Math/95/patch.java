    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
        if (d > 2.0) {
            // use mean
            ret = d / (d - 2.0);
        } else {
            // use 1.0 as a reasonable starting point when mean is undefined
            ret = 1.0;
        }
        return ret;
    }