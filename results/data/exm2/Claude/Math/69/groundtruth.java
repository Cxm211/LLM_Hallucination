// ===== FIXED org.apache.commons.math.stat.correlation.PearsonsCorrelation :: getCorrelationPValues() [lines 160-176] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-69-fixed/src/main/java/org/apache/commons/math/stat/correlation/PearsonsCorrelation.java =====
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
                    double t = Math.abs(r * Math.sqrt((nObs - 2)/(1 - r * r)));
                    out[i][j] = 2 * tDistribution.cumulativeProbability(-t);
                }
            }
        }
        return new BlockRealMatrix(out);
    }
