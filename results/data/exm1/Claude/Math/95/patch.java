protected double getInitialDomain(double p) {
    double ret;
    double d = getDenominatorDegreesOfFreedom();
    if (d > 2.0) {
        // use mean
        ret = d / (d - 2.0);
    } else {
        // For d <= 2, mean is undefined; use a different initial value
        ret = 1.0;
    }
    return ret;
}