// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java::testKeepInitIfBestMaximize
@Test
public void testKeepInitIfBestMaximize() {
    final double minSin = 3 * Math.PI / 2;
    final double offset = 1e-8;
    final double delta = 1e-7;
    final UnivariateFunction f1 = new Sin();
    // Create a small bump around the initial point within the search interval.
    final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 2 * offset },
                                                   new double[] { 0, +1, 0 });
    final UnivariateFunction f = FunctionUtils.add(f1, f2);
    final double relTol = 1e-8;
    final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-100);
    final double init = minSin + 1.5 * offset;
    final UnivariatePointValuePair result =
        optimizer.optimize(200, f, GoalType.MAXIMIZE,
                           minSin - 6.789 * delta,
                           minSin + 9.876 * delta,
                           init);

    final double sol = result.getPoint();
    final double expected = init;

    Assert.assertTrue("Best point not reported (max)", f.value(sol) >= f.value(expected));
}