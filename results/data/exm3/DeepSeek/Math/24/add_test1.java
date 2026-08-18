// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testMath855Quadratic() {
        final UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - 2.0) * (x - 2.0);
            }
        };
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        final UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 3.0);
        final double sol = result.getPoint();
        final double expected = 2.0;
        Assert.assertEquals(expected, sol, 1e-10);
    }
