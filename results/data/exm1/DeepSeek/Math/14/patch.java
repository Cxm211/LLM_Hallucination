private RealMatrix squareRoot(RealMatrix m) {
    final EigenDecomposition dec = new EigenDecomposition(m);
    final RealMatrix v = dec.getV();
    final RealMatrix d = dec.getD();
    final double[] sqrtD = new double[d.getRowDimension()];
    for (int i = 0; i < sqrtD.length; i++) {
        sqrtD[i] = FastMath.sqrt(d.getEntry(i, i));
    }
    return v.multiply(MatrixUtils.createRealDiagonalMatrix(sqrtD)).multiply(v.transpose());
}