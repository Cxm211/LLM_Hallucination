// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test(expected = NumberIsTooLargeException.class)
    public void testBoundaryRangeTooLargeMultipleDimensions() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    return parameters[0] * parameters[0] + parameters[1] * parameters[1];
                }
            };

        final double[] start = { 0, 0 };
        final double max = Double.MAX_VALUE / 2;
        final double tooLarge = FastMath.nextUp(max);
        final double[] lower = { -tooLarge, -100 };
        final double[] upper = { tooLarge, 100 };
        optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                          start, lower, upper);
    }