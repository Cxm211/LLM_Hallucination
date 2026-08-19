// ===== FIXED org.apache.commons.math.linear.ArrayRealVector :: getLInfNorm() [lines 718-724] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-77-fixed/src/main/java/org/apache/commons/math/linear/ArrayRealVector.java =====
    public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
            max = Math.max(max, Math.abs(a));
        }
        return max;
    }
