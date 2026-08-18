// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testMath855Maximize() {
        final double maxSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { maxSin, maxSin + offset, maxSin + 5 * offset },
                                                       new double[] { 0, 1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-100);
        final UnivariatePointValuePair result
            = optimizer.optimize(200, f, GoalType.MAXIMIZE, maxSin - 6.789 * delta, maxSin + 9.876 * delta);
        final double sol = result.getPoint();
        final double expected = maxSin + offset;
        Assert.assertTrue("Best point not reported", f.value(sol) >= f.value(expected));
    }
