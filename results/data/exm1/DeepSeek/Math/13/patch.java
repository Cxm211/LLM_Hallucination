private RealMatrix squareRoot(RealMatrix m) {
    final EigenDecomposition dec = new EigenDecomposition(m);
    final double[] eigenValues = dec.getRealEigenvalues();
    final RealMatrix eigenVectors = dec.getV();
    final int n = eigenValues.length;
    final RealMatrix sqrtD = new Array2DRowRealMatrix(n, n);
    for (int i = 0; i < n; i++) {
        final double lambda = eigenValues[i];
        if (lambda < 0) {
            sqrtD.setEntry(i, i, 0.0);
        } else {
            sqrtD.setEntry(i, i, FastMath.sqrt(lambda));
        }
    }
    return eigenVectors.multiply(sqrtD).multiply(eigenVectors.transpose());
}