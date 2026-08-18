public Weight(double[] weight) {
    final int dim = weight.length;
    weightMatrix = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(dim, dim);
    for (int i = 0; i < dim; i++) {
        if (weight[i] < 0) {
            throw new IllegalArgumentException("Weights must be non-negative");
        }
        weightMatrix.setEntry(i, i, weight[i]);
    }
}