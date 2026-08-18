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
        double sumExpected = 0.0d;
        double sumObserved = 0.0d;
        for (int i = 0; i < expected.length; i++) {
            sumExpected += expected[i];
            sumObserved += observed[i];
        }
        if (Math.abs(sumExpected - sumObserved) > 1e-10 * sumExpected) {
            throw new IllegalArgumentException(
                "sum of expected counts must equal sum of observed counts");
        }
        double sumSq = 0.0d;
        for (int i = 0; i < observed.length; i++) {
            double dev = ((double) observed[i] - expected[i]);
            sumSq += dev * dev / expected[i];
        }
        return sumSq;
    }