// org/apache/commons/math3/optim/nonlinear/scalar/noderiv/SimplexOptimizerNelderMeadTest.java
@Test
public void testIterationsBasic() {
    SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
    MultivariateFunction func = new MultivariateFunction() {
        public double value(double[] x) {
            return x[0] * x[0];
        }
    };
    PointValuePair optimum = optimizer.optimize(
        new MaxEval(100),
        new ObjectiveFunction(func),
        GoalType.MINIMIZE,
        new InitialGuess(new double[] { 5.0 }),
        new NelderMeadSimplex(new double[] { 0.5 }));
    Assert.assertTrue(optimizer.getIterations() > 0);
    Assert.assertTrue(Math.abs(optimum.getPoint()[0]) < 1e-5);
}