// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testMaximizationBestPoint() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x - 3.0) * (x - 3.0);
            }
        };
        final BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-30);
        final UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MAXIMIZE, 0.0, 6.0, 3.0);
        Assert.assertEquals(3.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }
