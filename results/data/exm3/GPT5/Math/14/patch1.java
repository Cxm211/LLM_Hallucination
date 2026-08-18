public Weight(double[] weight) {
        final int dim = weight.length;
        weightMatrix = new org.apache.commons.math3.linear.DiagonalMatrix(weight.clone());
    }