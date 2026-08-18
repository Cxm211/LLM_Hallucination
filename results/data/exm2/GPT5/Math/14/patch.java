private RealMatrix squareRoot(RealMatrix m) {
    if (m instanceof org.apache.commons.math3.linear.DiagonalMatrix) {
        org.apache.commons.math3.linear.DiagonalMatrix d = (org.apache.commons.math3.linear.DiagonalMatrix) m;
        final int dim = d.getRowDimension();
        final double[] diag = new double[dim];
        for (int i = 0; i < dim; i++) {
            diag[i] = Math.sqrt(d.getEntry(i, i));
        }
        return new org.apache.commons.math3.linear.DiagonalMatrix(diag);
    }
    final EigenDecomposition dec = new EigenDecomposition(m);
    return dec.getSquareRoot();
}