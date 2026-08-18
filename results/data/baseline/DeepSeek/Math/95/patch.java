protected double getInitialDomain(double p) {
    double ret;
    double d = getDenominatorDegreesOfFreedom();
    if (d <= 2.0) {
        ret = 1.0;
    } else {
        ret = d / (d - 2.0);
    }
    return ret;
}