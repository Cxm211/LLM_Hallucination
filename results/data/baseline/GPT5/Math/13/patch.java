private RealMatrix squareRoot(RealMatrix m) {
        final EigenDecomposition dec = new EigenDecomposition(m);
        final RealMatrix V = dec.getV();
        final RealMatrix D = dec.getD();
        final int n = D.getRowDimension();
        for (int i = 0; i < n; i++) {
            double eig = D.getEntry(i, i);
            if (eig < 0 && eig > -1e-12) {
                eig = 0.0;
            }
            D.setEntry(i, i, Math.sqrt(Math.max(0.0, eig)));
        }
        return V.multiply(D).multiply(V.transpose());
    }