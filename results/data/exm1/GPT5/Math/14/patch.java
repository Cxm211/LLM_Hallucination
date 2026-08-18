private RealMatrix squareRoot(RealMatrix m) {
    final CholeskyDecomposition dec = new CholeskyDecomposition(m);
    return dec.getLT();
}