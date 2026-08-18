// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testBracketRootAtLowerBound() throws org.apache.commons.math.ConvergenceException, org.apache.commons.math.FunctionEvaluationException {
        org.apache.commons.math.analysis.UnivariateRealFunction f = new org.apache.commons.math.analysis.UnivariateRealFunction() {
            public double value(double x) throws org.apache.commons.math.FunctionEvaluationException {
                return x - 0.0;
            }
        };
        double initial = 0.5;
        double lowerBound = 0.0;
        double upperBound = 10.0;
        int maxIterations = 100;
        double[] result = org.apache.commons.math.analysis.UnivariateRealSolverUtils.bracket(f, initial, lowerBound, upperBound, maxIterations);
        assertTrue(result[0] == lowerBound);
        assertTrue(result[1] > lowerBound);
    }
