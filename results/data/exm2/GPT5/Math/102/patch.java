public double chiSquare(double[] expected, long[] observed)
        throws IllegalArgumentException {
        if ((expected.length < 2) || (expected.length != observed.length)) {
            throw new IllegalArgumentException(
                    "observed, expected array lengths incorrect");
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] <= 0) {
                throw new IllegalArgumentException(
                    "observed counts must be non-negative and expected counts must be postive");
            }
            if (observed[i] < 0) {
                throw new IllegalArgumentException(
                    "observed counts must be non-negative and expected counts must be postive");
            }
        }
        double sumSq = 0.0d;
        double dev = 0.0d;
        for (int i = 0; i < observed.length; i++) {
                dev = ((double) observed[i] - expected[i]);
                sumSq += dev * dev / expected[i];
        }
        return sumSq;
    }