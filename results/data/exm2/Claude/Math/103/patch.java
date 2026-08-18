public double cumulativeProbability(double x) throws MathException {
    if (Double.isInfinite(standardDeviation)) {
        if (x < mean) {
            return 0.0;
        } else {
            return 1.0;
        }
    }
    return 0.5 * (1.0 + Erf.erf((x - mean) /
            (standardDeviation * Math.sqrt(2.0))));
}