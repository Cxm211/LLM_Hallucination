protected double getInitialDomain(double p) {
        double d = getDenominatorDegreesOfFreedom();
        // Use mean when it exists; otherwise fall back to a sensible positive value
        if (d > 2.0) {
            return d / (d - 2.0);
        }
        // For small degrees of freedom, the mean is undefined; start at 1.0
        return 1.0;
    }