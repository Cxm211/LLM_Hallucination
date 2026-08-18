// org/apache/commons/math3/optim/nonlinear/scalar/noderiv/CMAESOptimizerTest.java
@Test
public void testIterationsCountSimple() {
    double[] startPoint = new double[] { 1.0 };
    double[] insigma = new double[] { 0.1 };
    CMAESOptimizer optimizer = new CMAESOptimizer(100, 0.0, true, 0, 0,
        new MersenneTwister(), false, new SimpleValueChecker(1e-10, 1e-10));
    PointValuePair result = optimizer.optimize(
        new MaxEval(1000),
        new ObjectiveFunction(new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        }),
        GoalType.MINIMIZE,
        new InitialGuess(startPoint),
        new SimpleBounds(new double[] { -10 }, new double[] { 10 }),
        new CMAESOptimizer.Sigma(insigma),
        new CMAESOptimizer.PopulationSize(10));
    Assert.assertTrue(optimizer.getIterations() > 0);
    Assert.assertTrue(result.getPoint()[0] < 0.01);
}