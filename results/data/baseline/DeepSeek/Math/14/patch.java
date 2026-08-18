private RealMatrix squareRoot(RealMatrix m) {
    if (!org.apache.commons.math3.linear.MatrixUtils.isSymmetric(m, 1.0e-12)) {
        throw new org.apache.commons.math3.exception.NonSymmetricMatrixException(m.getRowDimension(), m.getColumnDimension());
    }
    final EigenDecomposition dec = new EigenDecomposition(m);
    return dec.getSquareRoot();
}