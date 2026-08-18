// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testBestPointIsStartWithAbsoluteValue() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.abs(x - 10.0);
            }
        };
        final BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-30);
        final UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 0.0, 20.0, 10.0);
        Assert.assertEquals(10.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }
