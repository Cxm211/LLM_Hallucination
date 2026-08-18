// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testBracketRootAtUpperBound() throws org.apache.commons.math.ConvergenceException, org.apache.commons.math.FunctionEvaluationException {
        org.apache.commons.math.analysis.UnivariateRealFunction f = new org.apache.commons.math.analysis.UnivariateRealFunction() {
            public double value(double x) throws org.apache.commons.math.FunctionEvaluationException {
                return 5.0 - x;
            }
        };
        double initial = 4.5;
        double lowerBound = 0.0;
        double upperBound = 5.0;
        int maxIterations = 100;
        double[] result = org.apache.commons.math.analysis.UnivariateRealSolverUtils.bracket(f, initial, lowerBound, upperBound, maxIterations);
        assertTrue(result[1] == upperBound);
        assertTrue(result[0] < upperBound);
    }
