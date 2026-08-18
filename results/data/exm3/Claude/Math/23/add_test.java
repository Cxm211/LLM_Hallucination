// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testKeepInitIfBestMaximize() {
        final double maxSin = Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { maxSin, maxSin + offset, maxSin + 2 * offset},
                                                       new double[] { 0, 1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        final double relTol = 1e-8;
        final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-100);
        final double init = maxSin + 1.5 * offset;
        final UnivariatePointValuePair result
            = optimizer.optimize(200, f, GoalType.MAXIMIZE,
                                 maxSin - 6.789 * delta,
                                 maxSin + 9.876 * delta,
                                 init);
        final double sol = result.getPoint();
        final double expected = init;
        Assert.assertTrue("Best point not reported", f.value(sol) >= f.value(expected));
    }