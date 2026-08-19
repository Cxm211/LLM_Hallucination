// ===== FIXED org.apache.commons.math.stat.descriptive.moment.Variance :: evaluate(double[], double[], double, int, int) [lines 501-532] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-41-fixed/src/main/java/org/apache/commons/math/stat/descriptive/moment/Variance.java =====
    public double evaluate(final double[] values, final double[] weights,
                           final double mean, final int begin, final int length) {

        double var = Double.NaN;

        if (test(values, weights, begin, length)) {
            if (length == 1) {
                var = 0.0;
            } else if (length > 1) {
                double accum = 0.0;
                double dev = 0.0;
                double accum2 = 0.0;
                for (int i = begin; i < begin + length; i++) {
                    dev = values[i] - mean;
                    accum += weights[i] * (dev * dev);
                    accum2 += weights[i] * dev;
                }

                double sumWts = 0;
                for (int i = begin; i < begin + length; i++) {
                    sumWts += weights[i];
                }

                if (isBiasCorrected) {
                    var = (accum - (accum2 * accum2 / sumWts)) / (sumWts - 1.0);
                } else {
                    var = (accum - (accum2 * accum2 / sumWts)) / sumWts;
                }
            }
        }
        return var;
    }
