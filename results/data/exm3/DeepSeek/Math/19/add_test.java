// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
public void testBoundaryRangeTooLargeWithSigma() {
    final CMAESOptimizer optimizer = new CMAESOptimizer();
    final MultivariateFunction fitnessFunction = new MultivariateFunction() {
        public double value(double[] parameters) {
            return 0.0;
        }
    };
    final double[] start = { 0 };
    final double max = Double.MAX_VALUE / 2;
    final double tooLarge = FastMath.nextUp(max);
    final double[] lower = { -tooLarge };
    final double[] upper = { tooLarge };
    final double[] sigma = { 1.0 };
    try {
        optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE, start, lower, upper, sigma);
        Assert.fail("Expected MathUnsupportedOperationException");
    } catch (MathUnsupportedOperationException e) {
        // expected
    }
}
