public double cumulativeProbability(double x) throws MathException {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x == Double.NEGATIVE_INFINITY) {
            return 0.0;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 1.0;
        }
        final double dev = x - mean;
        // Short-circuit for extreme tails to avoid numerical issues in Erf
        if (dev < -20.0 * standardDeviation) {
            return 0.0;
        }
        if (dev > 20.0 * standardDeviation) {
            return 1.0;
        }
        try {
            return 0.5 * (1.0 + Erf.erf(dev / (standardDeviation * FastMath.sqrt(2.0))))
;        } catch (MaxIterationsExceededException ex) {
            // As a safe fallback, top-code according to the sign relative to the mean
            return (x < mean) ? 0.0 : 1.0;
        }
    }