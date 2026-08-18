// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test(expected = NumberIsTooLargeException.class)
    public void testBoundaryRangeNaN() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    return parameters[0] * parameters[0];
                }
            };

        final double[] start = { 0 };
        final double[] lower = { -Double.MAX_VALUE };
        final double[] upper = { Double.MAX_VALUE };
        optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                          start, lower, upper);
    }