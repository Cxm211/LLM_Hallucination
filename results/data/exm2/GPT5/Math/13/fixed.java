// ===== FIXED org.apache.commons.math3.optimization.general.AbstractLeastSquaresOptimizer :: squareRoot(RealMatrix) [lines 561-573] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-13-fixed/src/main/java/org/apache/commons/math3/optimization/general/AbstractLeastSquaresOptimizer.java =====
    private RealMatrix squareRoot(RealMatrix m) {
        if (m instanceof DiagonalMatrix) {
            final int dim = m.getRowDimension();
            final RealMatrix sqrtM = new DiagonalMatrix(dim);
            for (int i = 0; i < dim; i++) {
               sqrtM.setEntry(i, i, FastMath.sqrt(m.getEntry(i, i)));
            }
            return sqrtM;
        } else {
            final EigenDecomposition dec = new EigenDecomposition(m);
            return dec.getSquareRoot();
        }
    }
