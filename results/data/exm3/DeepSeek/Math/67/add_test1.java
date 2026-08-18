// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java
@Test
    public void testMultiStartMaxLocal() throws MathException {
        // Custom function with global maximum at x=1, value 5, and local maximum at x=4, value 3
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                if (x <= 2.5) {
                    return 5 - (x - 1) * (x - 1);
                } else {
                    return 3 - (x - 4) * (x - 4);
                }
            }
        };
        UnivariateRealOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator g = new JDKRandomGenerator() {
            private double[] values = {0.2, 0.9};
            private int index = 0;
            @Override
            public double nextDouble() {
                return values[index++];
            }
        };
        MultiStartUnivariateRealOptimizer maximizer = new MultiStartUnivariateRealOptimizer(underlying, 2, g);
        maximizer.setAbsoluteAccuracy(1e-10);
        maximizer.setRelativeAccuracy(1e-10);
        double result = maximizer.optimize(f, GoalType.MAXIMIZE, 0, 5);
        // Global maximum is at x=1, value 5
        assertEquals(1.0, result, 1e-9);
        assertEquals(1.0, maximizer.getResult(), 1e-9);
        assertEquals(5.0, maximizer.getFunctionValue(), 1e-9);
        double[] optima = maximizer.getOptima();
        double[] optimaValues = maximizer.getOptimaValues();
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(f.value(optima[i]), optimaValues[i], 1e-9);
        }
    }
