// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java
@Test
    public void testMultiStartMinLocal() throws MathException {
        // Custom function with global minimum at x=0, value 0, and local minimum at x=3, value 2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                if (x <= 1.5) {
                    return x * x;
                } else {
                    return (x - 3) * (x - 3) + 2;
                }
            }
        };
        UnivariateRealOptimizer underlying = new BrentOptimizer();
        // Use a controlled random generator that returns specific values for two starts
        JDKRandomGenerator g = new JDKRandomGenerator() {
            private double[] values = {0.1, 0.8};
            private int index = 0;
            @Override
            public double nextDouble() {
                return values[index++];
            }
        };
        MultiStartUnivariateRealOptimizer minimizer = new MultiStartUnivariateRealOptimizer(underlying, 2, g);
        minimizer.setAbsoluteAccuracy(1e-10);
        minimizer.setRelativeAccuracy(1e-10);
        // Optimize over interval that contains both minima
        double result = minimizer.optimize(f, GoalType.MINIMIZE, -1, 4);
        // Global minimum is at x=0, value 0
        assertEquals(0.0, result, 1e-9);
        assertEquals(0.0, minimizer.getResult(), 1e-9);
        assertEquals(0.0, minimizer.getFunctionValue(), 1e-9);
        // Check that optima and optimaValues are consistent
        double[] optima = minimizer.getOptima();
        double[] optimaValues = minimizer.getOptimaValues();
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(f.value(optima[i]), optimaValues[i], 1e-9);
        }
    }
