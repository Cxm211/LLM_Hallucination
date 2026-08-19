// ===== FIXED org.apache.commons.math.distribution.FDistributionImpl :: getInitialDomain(double) [lines 143-151] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-95-fixed/src/java/org/apache/commons/math/distribution/FDistributionImpl.java =====
    protected double getInitialDomain(double p) {
        double ret = 1.0;
        double d = getDenominatorDegreesOfFreedom();
        if (d > 2.0) {
            // use mean
            ret = d / (d - 2.0);
        }
        return ret;
    }
