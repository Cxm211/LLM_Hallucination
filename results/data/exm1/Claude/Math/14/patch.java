private RealMatrix squareRoot(RealMatrix m) {
    final int dim = m.getRowDimension();
    RealMatrix sqrtM = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(dim, dim);
    for (int i = 0; i < dim; i++) {
        sqrtM.setEntry(i, i, org.apache.commons.math3.util.FastMath.sqrt(m.getEntry(i, i)));
    }
    return sqrtM;
}