// ===== FIXED org.apache.commons.math3.optim.nonlinear.vector.Weight :: Weight [lines 41-44] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-14-fixed/src/main/java/org/apache/commons/math3/optim/nonlinear/vector/Weight.java =====
    public Weight(double[] weight) {
        final int dim = weight.length;
        weightMatrix = new DiagonalMatrix(weight);
    }

// ===== FIXED org.apache.commons.math3.optim.nonlinear.vector.jacobian.AbstractLeastSquaresOptimizer :: squareRoot(RealMatrix) [lines 266-278] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-14-fixed/src/main/java/org/apache/commons/math3/optim/nonlinear/vector/jacobian/AbstractLeastSquaresOptimizer.java =====
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
