public RealMatrix getCorrelationPValues() throws MathException {
        TDistribution tDistribution = new TDistributionImpl(nObs - 2);
        int nVars = correlationMatrix.getColumnDimension();
        double[][] out = new double[nVars][nVars];
        for (int i = 0; i < nVars; i++) {
            for (int j = 0; j < nVars; j++) {
                if (i == j) {
                    out[i][j] = 0d;
                } else {
                    double r = correlationMatrix.getEntry(i, j);
                    // Clamp r to [-1, 1] to avoid numerical issues
                    if (r > 1d) {
                        r = 1d;
                    } else if (r < -1d) {
                        r = -1d;
                    }
                    if (Math.abs(r) == 1d) {
                        out[i][j] = 0d;
                    } else {
                        double t = Math.abs(r) * Math.sqrt((nObs - 2d) / (1d - r * r));
                        out[i][j] = 2 * (1 - tDistribution.cumulativeProbability(t));
                    }
                }
            }
        }
        return new BlockRealMatrix(out);
    }