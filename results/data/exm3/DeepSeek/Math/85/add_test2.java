// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testBracketRootAtInitialPlusOne() throws org.apache.commons.math.ConvergenceException, org.apache.commons.math.FunctionEvaluationException {
        org.apache.commons.math.analysis.UnivariateRealFunction f = new org.apache.commons.math.analysis.UnivariateRealFunction() {
            public double value(double x) throws org.apache.commons.math.FunctionEvaluationException {
                return x - 2.0;
            }
        };
        double initial = 1.0;
        double lowerBound = -10.0;
        double upperBound = 10.0;
        int maxIterations = 100;
        double[] result = org.apache.commons.math.analysis.UnivariateRealSolverUtils.bracket(f, initial, lowerBound, upperBound, maxIterations);
        assertEquals(2.0, result[1], 1.0e-12);
        assertTrue(result[0] < result[1]);
    }
