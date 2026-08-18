private RealMatrix squareRoot(RealMatrix m) {
        // Fast path for diagonal weight matrices (common in curve fitting):
        if (m instanceof org.apache.commons.math3.linear.DiagonalMatrix) {
            double[] diag = ((org.apache.commons.math3.linear.DiagonalMatrix) m).getDiagonal().clone();
            for (int i = 0; i < diag.length; i++) {
                if (diag[i] < 0) {
                    throw new org.apache.commons.math3.exception.NonPositiveDefiniteMatrixException();
                }
                diag[i] = org.apache.commons.math3.util.FastMath.sqrt(diag[i]);
            }
            return new org.apache.commons.math3.linear.DiagonalMatrix(diag);
        }
        // Prefer Cholesky for SPD matrices (more efficient and numerically stable).
        try {
            org.apache.commons.math3.linear.CholeskyDecomposition chol = new org.apache.commons.math3.linear.CholeskyDecomposition(m, 1e-15, 1e-15);
            // Return a factor R such that R^T R = m. Using L^T from Cholesky (m = L L^T).
            return chol.getLT();
        } catch (org.apache.commons.math3.exception.NonPositiveDefiniteMatrixException ex) {
            // Fallback to eigen decomposition when Cholesky is not applicable.
            final org.apache.commons.math3.linear.EigenDecomposition dec = new org.apache.commons.math3.linear.EigenDecomposition(m);
            return dec.getSquareRoot();
        }
    }