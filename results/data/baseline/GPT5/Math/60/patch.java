public double cumulativeProbability(double x) throws MathException {
        final double dev = x - mean;
        if (x < (mean - 20 * standardDeviation)) {
            return 0;
        }
        if (x > (mean + 20 * standardDeviation)) {
            return 1;
        }
        try {
            return 0.5 * (1.0 + Erf.erf(dev / (standardDeviation * FastMath.sqrt(2.0))));
        } catch (MaxIterationsExceededException ex) {
            throw ex;
        }
    }