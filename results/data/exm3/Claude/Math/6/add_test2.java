// org/apache/commons/math3/optim/nonlinear/scalar/noderiv/PowellOptimizerTest.java
@Test
public void testIterationsCount() {
    final MultivariateFunction func = new MultivariateFunction() {
        public double value(double[] x) {
            return (x[0] - 3) * (x[0] - 3) + (x[1] + 2) * (x[1] + 2);
        }
    };
    PowellOptimizer optimizer = new PowellOptimizer(1e-9, 1e-9);
    PointValuePair result = optimizer.optimize(
        new MaxEval(1000),
        new ObjectiveFunction(func),
        GoalType.MINIMIZE,
        new InitialGuess(new double[] { 0, 0 }));
    Assert.assertTrue(optimizer.getIterations() > 0);
    Assert.assertEquals(3.0, result.getPoint()[0], 1e-5);
    Assert.assertEquals(-2.0, result.getPoint()[1], 1e-5);
}