public double cumulativeProbability(double x) throws MathException {
    if (x < mean - 20 * standardDeviation) {
        return 0;
    } else if (x > mean + 20 * standardDeviation) {
        return 1;
    }
    final double dev = x - mean;
    try {
        return 0.5 * (1.0 + Erf.erf(dev / (standardDeviation * FastMath.sqrt(2.0))));
    } catch (MaxIterationsExceededException ex) {
        throw ex;
    }
}