public double cumulativeProbability(double x) throws MathException {
        double z = (x - mean) / (standardDeviation * Math.sqrt(2.0));
        if (z > 40) {
            return 1.0;
        } else if (z < -40) {
            return 0.0;
        } else {
            return 0.5 * (1.0 + Erf.erf(z));
        }
    }