// ===== FIXED org.apache.commons.math.stat.regression.SimpleRegression :: getSumSquaredErrors() [lines 263-265] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-105-fixed/src/java/org/apache/commons/math/stat/regression/SimpleRegression.java =====
    public double getSumSquaredErrors() {
        return Math.max(0d, sumYY - sumXY * sumXY / sumXX);
    }
