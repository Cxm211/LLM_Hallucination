private RealMatrix squareRoot(RealMatrix m) {
    // Ensure symmetry to avoid issues with numerical asymmetry
    final RealMatrix symm = m.add(m.transpose()).scalarMultiply(0.5);
    final EigenDecomposition dec = new EigenDecomposition(symm);
    final int n = symm.getRowDimension();
    final RealMatrix V = dec.getV();

    final double[] sqrtEigen = new double[n];
    double maxEig = 0.0;
    for (int i = 0; i < n; i++) {
        maxEig = Math.max(maxEig, Math.abs(dec.getRealEigenvalue(i)));
    }
    final double tol = Math.max(0.0, 1e-15 * maxEig);

    for (int i = 0; i < n; i++) {
        double lambda = dec.getRealEigenvalue(i);
        if (lambda < -tol) {
            throw new NonPositiveDefiniteMatrixException(lambda, i, 0);
        }
        if (lambda < 0) {
            lambda = 0.0; // clip tiny negative due to numerical noise
        }
        sqrtEigen[i] = Math.sqrt(lambda);
    }

    final RealMatrix Dsqrt = MatrixUtils.createRealDiagonalMatrix(sqrtEigen);
    return V.multiply(Dsqrt).multiply(V.transpose());
}