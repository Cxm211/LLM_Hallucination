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
                    // Clip r to [-1,1] to avoid numerical issues
                    if (r > 1.0) {
                        r = 1.0;
                    } else if (r < -1.0) {
                        r = -1.0;
                    }
                    double denom = 1.0 - r * r;
                    double t;
                    if (denom <= 0) {
                        // If r is +/-1, t is infinite, p-value is 0
                        out[i][j] = 0d;
                        continue;
                    } else {
                        t = Math.abs(r * Math.sqrt((nObs - 2) / denom));
                    }
                    // Use cumulativeProbability(-t) for better numerical stability
                    out[i][j] = 2 * tDistribution.cumulativeProbability(-t);
                }
            }
        }
        return new BlockRealMatrix(out);
    }