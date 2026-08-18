public Weight(double[] weight) {
    final int dim = weight.length;
    weightMatrix = org.apache.commons.math3.linear.MatrixUtils.createRealDiagonalMatrix(weight);
}