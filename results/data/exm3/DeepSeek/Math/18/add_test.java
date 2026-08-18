// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
    public void testOptimizationWithFixedDimension() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                final double target = 10.0;
                final double error = target - parameters[1];
                return error * error;
            }
        };
        final double[] start = { 5.0, 0.0 };
        final double[] lower = { 5.0, -100 };
        final double[] upper = { 5.0, 100 };
        PointValuePair result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                                   start, lower, upper);
        Assert.assertEquals(5.0, result.getPoint()[0], 1e-14);
        Assert.assertEquals(10.0, result.getPoint()[1], 1e-6);
    }
