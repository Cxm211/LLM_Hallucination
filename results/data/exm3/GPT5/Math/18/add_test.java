// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
    public void testZeroRangeBound() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 3.0;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 0.0 };
        final double[] lower = { 2.0 };
        final double[] upper = { 2.0 };

        PointValuePair result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                                   start, lower, upper);
        Assert.assertArrayEquals(new double[] { 2.0 }, result.getPoint(), 1e-9);
    }