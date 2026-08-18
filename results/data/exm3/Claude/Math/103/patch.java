public double cumulativeProbability(double x) throws MathException {
    if (Double.isNaN(x)) {
        throw new MathException("Input value is NaN");
    }
    if (standardDeviation == 0) {
        if (x < mean) {
            return 0.0;
        } else if (x > mean) {
            return 1.0;
        } else {
            return 0.5;
        }
    }
    return 0.5 * (1.0 + Erf.erf((x - mean) / (standardDeviation * Math.sqrt(2.0))));
}