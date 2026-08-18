// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testBoundsRespected() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 0.5) * (x - 0.5);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(1e-12);
        optimizer.setAbsoluteAccuracy(1e-12);
        double min = 0.0;
        double max = 1.0;
        double result = optimizer.optimize(f, GoalType.MINIMIZE, min, max);
        assertTrue("Result " + result + " not in [" + min + "," + max + "]", result >= min && result <= max);
        assertEquals(0.5, result, 1e-8);
    }
