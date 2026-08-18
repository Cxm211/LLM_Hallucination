// ===== FIXED org.apache.commons.math3.distribution.MultivariateNormalDistribution :: density(double[]) [lines 177-186] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-11-fixed/src/main/java/org/apache/commons/math3/distribution/MultivariateNormalDistribution.java =====
    public double density(final double[] vals) throws DimensionMismatchException {
        final int dim = getDimension();
        if (vals.length != dim) {
            throw new DimensionMismatchException(vals.length, dim);
        }

        return FastMath.pow(2 * FastMath.PI, -0.5 * dim) *
            FastMath.pow(covarianceMatrixDeterminant, -0.5) *
            getExponentTerm(vals);
    }
