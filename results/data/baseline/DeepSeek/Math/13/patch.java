    private RealMatrix squareRoot(RealMatrix m) {
            RealMatrix symmetric = m.add(m.transpose()).scalarMultiply(0.5);
            final EigenDecomposition dec = new EigenDecomposition(symmetric);
            return dec.getSquareRoot();
    }