// ===== FIXED org.apache.commons.math.stat.inference.ChiSquareTestImpl :: chiSquare(double[], long[]) [lines 64-98] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-102-fixed/src/java/org/apache/commons/math/stat/inference/ChiSquareTestImpl.java =====
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
        double sumExpected = 0d;
        double sumObserved = 0d;
        for (int i = 0; i < observed.length; i++) {
            sumExpected += expected[i];
            sumObserved += observed[i];
        }
        double ratio = 1.0d;
        boolean rescale = false;
        if (Math.abs(sumExpected - sumObserved) > 10E-6) {
            ratio = sumObserved / sumExpected;
            rescale = true;
        }
        double sumSq = 0.0d;
        double dev = 0.0d;
        for (int i = 0; i < observed.length; i++) {
            if (rescale) {
                dev = ((double) observed[i] - ratio * expected[i]);
                sumSq += dev * dev / (ratio * expected[i]);
            } else {
                dev = ((double) observed[i] - expected[i]);
                sumSq += dev * dev / expected[i];
            }
        }
        return sumSq;
    }
