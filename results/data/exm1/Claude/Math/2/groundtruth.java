// ===== FIXED org.apache.commons.math3.distribution.HypergeometricDistribution :: getNumericalMean() [lines 267-269] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-2-fixed/src/main/java/org/apache/commons/math3/distribution/HypergeometricDistribution.java =====
    public double getNumericalMean() {
        return getSampleSize() * (getNumberOfSuccesses() / (double) getPopulationSize());
    }
