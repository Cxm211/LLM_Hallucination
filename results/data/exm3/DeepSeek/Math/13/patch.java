    private RealMatrix squareRoot(RealMatrix m) {
            final EigenDecomposition dec = new EigenDecomposition(m);
            // Safe computation of square root by clipping negative eigenvalues
            RealMatrix V = dec.getV();
            RealMatrix D = dec.getD();
            int n = m.getRowDimension();
            RealMatrix sqrtD = MatrixUtils.createRealDiagonalMatrix(new double[n]);
            for (int i = 0; i < n; i++) {
                double eval = D.getEntry(i, i);
                if (eval > 0) {
                    sqrtD.setEntry(i, i, Math.sqrt(eval));
                } else {
                    sqrtD.setEntry(i, i, 0.0);
                }
            }
            RealMatrix VT = dec.getVT();
            return V.multiply(sqrtD).multiply(VT);
    }