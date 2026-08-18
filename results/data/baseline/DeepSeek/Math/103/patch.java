    public double cumulativeProbability(double x) throws MathException {
        return 0.5 * Erf.erfc((mean - x) / (standardDeviation * Math.sqrt(2.0)));
    }