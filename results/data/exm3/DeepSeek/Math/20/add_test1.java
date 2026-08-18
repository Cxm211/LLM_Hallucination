// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
    public void testBothBounds2D() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                @Override
                public double value(double[] parameters) {
                    final double x = parameters[0];
                    final double y = parameters[1];
                    return (x - 10) * (x - 10) + (y + 10) * (y + 10);
                }
            };

        final double[] start = { 2, -2 };
        final double[] lower = { 0, -5 };
        final double[] upper = { 5, 0 };
        final double[] result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                                   start, lower, upper).getPoint();
        Assert.assertTrue("x out of bounds (" + result[0] + " > " + upper[0] + " || " + result[0] + " < " + lower[0] + ")",
                          result[0] <= upper[0] && result[0] >= lower[0]);
        Assert.assertTrue("y out of bounds (" + result[1] + " > " + upper[1] + " || " + result[1] + " < " + lower[1] + ")",
                          result[1] <= upper[1] && result[1] >= lower[1]);
    }
