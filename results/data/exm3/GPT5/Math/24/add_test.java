// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testConvergenceCheckerReturnsBestPoint() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return (x - 1) * (x - 1); }
        };
        final ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                // Force early termination at first opportunity.
                return true;
            }
        };
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        final UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0, 2);
        Assert.assertEquals(1.0, result.getPoint(), 1e-8);
    }