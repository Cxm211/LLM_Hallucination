    private RealMatrix squareRoot(RealMatrix m) {
            // Ensure the matrix is symmetric
            RealMatrix sym = m.add(m.transpose()).scalarMultiply(0.5);
            final EigenDecomposition dec = new EigenDecomposition(sym);
            return dec.getSquareRoot();
    }