// org/apache/commons/math3/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testInitBestWithEarlyConvergence() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (Math.abs(x - 0.5) < 1e-10) {
                    return 0.0;
                }
                return Math.abs(x - 0.5) + 0.1;
            }
        };
        final double relTol = 1e-10;
        final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-14);
        final double init = 0.5;
        final UnivariatePointValuePair result
            = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 1.0, init);
        final double sol = result.getPoint();
        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(init) + 1e-10);
    }