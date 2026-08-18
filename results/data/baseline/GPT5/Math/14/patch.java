private RealMatrix squareRoot(RealMatrix m) {
        final EigenDecomposition dec = new EigenDecomposition(m);
        final double[] evals = dec.getRealEigenvalues();
        final double[] sqrtEvals = new double[evals.length];
        for (int i = 0; i < evals.length; i++) {
            sqrtEvals[i] = Math.sqrt(Math.max(0.0, evals[i]));
        }
        final RealMatrix V = dec.getV();
        final RealMatrix sqrtD = org.apache.commons.math3.linear.MatrixUtils.createRealDiagonalMatrix(sqrtEvals);
        return V.multiply(sqrtD).multiply(V.transpose());
    }