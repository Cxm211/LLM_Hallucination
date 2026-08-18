// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
public void testDefaultTerminationReturnsBest() {
    final UnivariateFunction f = new UnivariateFunction() {
        public double value(double x) {
            return Math.abs(x - 0.5);
        }
    };
    
    final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
    final UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 0.0, 1.0, 0.3);
    
    // Should return the best point found, not necessarily the last point evaluated
    Assert.assertTrue("Best point should be near minimum", Math.abs(result.getPoint() - 0.5) < 1e-8);
    Assert.assertTrue("Best value should be near zero", result.getValue() < 1e-8);
}