    public double chiSquare(double[] expected, long[] observed)
        throws IllegalArgumentException {
        if ((expected.length < 2) || (expected.length != observed.length)) {
            throw new IllegalArgumentException(
                    "observed, expected array lengths incorrect");
        }
        if (!isPositive(expected) || !isNonNegative(observed)) {
            throw new IllegalArgumentException(
                "observed counts must be non-negative and expected counts must be positive");
        }
        double sumObserved = 0.0;
        for (int i = 0; i < observed.length; i++) {
            sumObserved += observed[i];
        }
        if (sumObserved == 0.0) {
            throw new IllegalArgumentException(
                "observed counts sum to zero, no data");
        }
        double sumExpected = 0.0;
        for (int i = 0; i < expected.length; i++) {
            sumExpected += expected[i];
        }
        double ratio = sumObserved / sumExpected;
        double sumSq = 0.0;
        for (int i = 0; i < observed.length; i++) {
            double adjustedExpected = expected[i] * ratio;
            double dev = (double) observed[i] - adjustedExpected;
            sumSq += dev * dev / adjustedExpected;
        }
        return sumSq;
    }