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
        double sumExpected = 0.0;
        double sumObserved = 0.0;
        for (int i = 0; i < observed.length; i++) {
            sumExpected += expected[i];
            sumObserved += observed[i];
        }
        double ratio = 1.0;
        boolean rescale = false;
        if (Math.abs(sumExpected - sumObserved) > 1E-6 * Math.max(sumExpected, sumObserved)) {
            ratio = sumObserved / sumExpected;
            rescale = true;
        }
        double sumSq = 0.0;
        double dev = 0.0;
        for (int i = 0; i < observed.length; i++) {
            if (rescale) {
                dev = (double) observed[i] - ratio * expected[i];
                sumSq += dev * dev / (ratio * expected[i]);
            } else {
                dev = (double) observed[i] - expected[i];
                sumSq += dev * dev / expected[i];
            }
        }
        return sumSq;
    }