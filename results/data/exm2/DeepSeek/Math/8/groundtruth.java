// ===== FIXED org.apache.commons.math3.distribution.DiscreteDistribution :: sample(int) [lines 181-195] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-8-fixed/src/main/java/org/apache/commons/math3/distribution/DiscreteDistribution.java =====
    public Object[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        final Object[] out = new Object[sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }

        return out;

    }
