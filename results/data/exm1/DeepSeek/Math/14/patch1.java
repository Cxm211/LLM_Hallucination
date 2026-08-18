public Weight(double[] weight) {
    weightMatrix = org.apache.commons.math3.linear.MatrixUtils.createRealDiagonalMatrix(weight);
}