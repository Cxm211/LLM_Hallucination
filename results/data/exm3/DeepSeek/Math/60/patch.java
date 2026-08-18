    public double cumulativeProbability(double x) throws MathException {
        final double dev = x - mean;
        if (standardDeviation == 0) {
            return (x < mean) ? 0.0 : 1.0;
        }
        final double sqrt2 = FastMath.sqrt(2.0);
        final double z = dev / (standardDeviation * sqrt2);
        if (Double.isInfinite(z)) {
            // erf of +/-inf is +/-1
            return (z > 0) ? 1.0 : 0.0;
        }
        if (Double.isNaN(z)) {
            return Double.NaN;
        }
        try {
            return 0.5 * (1.0 + Erf.erf(z));
        } catch (MaxIterationsExceededException ex) {
            if (x < (mean - 20 * standardDeviation)) { // JDK 1.5 blows at 38
                return 0;
            } else if (x > (mean + 20 * standardDeviation)) {
                return 1;
            } else {
                throw ex;
            }
        }
    }