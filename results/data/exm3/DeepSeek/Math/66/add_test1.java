// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testMaximizeQuadratic() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -(x - 0.5) * (x - 0.5);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(1e-12);
        optimizer.setAbsoluteAccuracy(1e-12);
        double min = 0.0;
        double max = 1.0;
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, min, max);
        assertTrue("Result " + result + " not in [" + min + "," + max + "]", result >= min && result <= max);
        // Maximum of -(x-0.5)^2 is at boundaries (0 or 1) since it's concave down.
        // The optimizer should converge to one of the boundaries.
        assertTrue(result == 0.0 || result == 1.0);
    }
