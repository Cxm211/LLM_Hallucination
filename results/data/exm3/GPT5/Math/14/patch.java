private RealMatrix squareRoot(RealMatrix m) {
        if (m instanceof org.apache.commons.math3.linear.DiagonalMatrix) {
            final double[] diag = ((org.apache.commons.math3.linear.DiagonalMatrix) m).getDiagonal();
            final double[] sqrt = new double[diag.length];
            for (int i = 0; i < diag.length; i++) {
                sqrt[i] = FastMath.sqrt(diag[i]);
            }
            return new org.apache.commons.math3.linear.DiagonalMatrix(sqrt);
        }
        final EigenDecomposition dec = new EigenDecomposition(m);
        return dec.getSquareRoot();
    }