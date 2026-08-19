// ===== FIXED org.apache.commons.math3.stat.inference.MannWhitneyUTest :: calculateAsymptoticPValue(double, int, int) [lines 168-184] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-30-fixed/src/main/java/org/apache/commons/math3/stat/inference/MannWhitneyUTest.java =====
    private double calculateAsymptoticPValue(final double Umin,
                                             final int n1,
                                             final int n2)
        throws ConvergenceException, MaxCountExceededException {

        final double n1n2prod = n1 * n2;

        // http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U#Normal_approximation
        final double EU = n1n2prod / 2.0;
        final double VarU = n1n2prod * (n1 + n2 + 1) / 12.0;

        final double z = (Umin - EU) / FastMath.sqrt(VarU);

        final NormalDistribution standardNormal = new NormalDistribution(0, 1);

        return 2 * standardNormal.cumulativeProbability(z);
    }
