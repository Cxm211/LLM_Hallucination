// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
public void testConvergenceCheckerReturnsBest() {
    final UnivariateFunction f = new UnivariateFunction() {
        public double value(double x) {
            return (x - 1) * (x - 1);
        }
    };
    
    final UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
    
    // Use a convergence checker that stops after 5 iterations
    ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
        private int count = 0;
        public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
            count++;
            return count >= 5;
        }
    };
    
    final UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 0.0, 2.0, 1.5, checker);
    
    // The result should be the best of current and previous when checker signals convergence
    Assert.assertTrue("Result should have value close to minimum", result.getValue() <= 0.1);
}