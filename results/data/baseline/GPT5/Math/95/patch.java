protected double getInitialDomain(double p) {
    double d = getDenominatorDegreesOfFreedom();
    if (d > 2.0) {
        return d / (d - 2.0);
    }
    return 1.0;
}