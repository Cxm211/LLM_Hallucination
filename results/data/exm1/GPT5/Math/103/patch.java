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
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation must be positive");
        }
        return 0.5 * (1.0 + Erf.erf((x - mean) / (standardDeviation * Math.sqrt(2.0))));
    }