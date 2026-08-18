// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
    public void testOptimizationAtBoundaryWithRoundingError() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final double lower = 0.1;
        final double upper = 0.2;
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                final double target = upper;
                final double error = target - parameters[0];
                return error * error;
            }
        };
        final double[] start = { 0.15 };
        PointValuePair result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                                   start, new double[]{lower}, new double[]{upper});
        Assert.assertEquals(upper, result.getPoint()[0], 1e-8);
    }
