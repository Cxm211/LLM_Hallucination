    private RealMatrix squareRoot(RealMatrix m) {
        final EigenDecomposition dec = new EigenDecomposition(m);
        RealMatrix d = dec.getD();
        RealMatrix v = dec.getV();
        int n = m.getRowDimension();
        RealMatrix sqrtD = org.apache.commons.math3.linear.MatrixUtils.createRealDiagonalMatrix(new double[n]);
        for (int i = 0; i < n; i++) {
            double eig = d.getEntry(i, i);
            sqrtD.setEntry(i, i, Math.sqrt(Math.max(0, eig)));
        }
        return v.multiply(sqrtD).multiply(v.transpose());
    }