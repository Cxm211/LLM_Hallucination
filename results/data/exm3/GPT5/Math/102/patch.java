public double chiSquare(double[] expected, long[] observed)
        throws IllegalArgumentException {
        if ((expected.length < 2) || (expected.length != observed.length)) {
            throw new IllegalArgumentException(
                    "observed, expected array lengths incorrect");
        }
        if (!isPositive(expected) || !isNonNegative(observed)) {
            throw new IllegalArgumentException(
                "observed counts must be non-negative and expected counts must be postive");
        }
        double sumExpected = 0.0d;
        long sumObserved = 0L;
        for (int i = 0; i < expected.length; i++) {
            sumExpected += expected[i];
            sumObserved += observed[i];
        }
        double ratio = sumExpected == 0.0d ? 1.0d : ((double) sumObserved) / sumExpected;
        double sumSq = 0.0d;
        for (int i = 0; i < observed.length; i++) {
            double ei = expected[i] * ratio;
            double dev = ((double) observed[i] - ei);
            sumSq += dev * dev / ei;
        }
        return sumSq;
    }