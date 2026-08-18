public double cumulativeProbability(double x) throws MathException {
    double dev = (x - mean) / (standardDeviation * Math.sqrt(2.0));
    if (dev > 40.0) {
        return 1.0;
    } else if (dev < -40.0) {
        return 0.0;
    }
    return 0.5 * (1.0 + Erf.erf(dev));
}