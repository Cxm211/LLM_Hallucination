protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
        if (d > 2.0) {
            // use mean
            ret = d / (d - 2.0);
        } else {
            // fallback when mean is undefined (d <= 2)
            ret = 1.0;
        }
        return ret;
    }