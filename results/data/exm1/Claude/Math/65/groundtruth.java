// ===== FIXED org.apache.commons.math.optimization.general.AbstractLeastSquaresOptimizer :: getChiSquare() [lines 249-256] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-65-fixed/src/main/java/org/apache/commons/math/optimization/general/AbstractLeastSquaresOptimizer.java =====
    public double getChiSquare() {
        double chiSquare = 0;
        for (int i = 0; i < rows; ++i) {
            final double residual = residuals[i];
            chiSquare += residual * residual * residualsWeights[i];
        }
        return chiSquare;
    }

// ===== FIXED org.apache.commons.math.optimization.general.AbstractLeastSquaresOptimizer :: getRMS() [lines 239-241] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-65-fixed/src/main/java/org/apache/commons/math/optimization/general/AbstractLeastSquaresOptimizer.java =====
    public double getRMS() {
        return Math.sqrt(getChiSquare() / rows);
    }
