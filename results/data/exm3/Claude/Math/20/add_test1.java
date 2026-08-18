// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
public void testRepairBothBounds() {
    final CMAESOptimizer optimizer = new CMAESOptimizer();
    final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            @Override
            public double value(double[] parameters) {
                return parameters[0] * parameters[0] + parameters[1] * parameters[1];
            }
        };

    final double[] start = { 5, 5 };
    final double[] lower = { -2, -3 };
    final double[] upper = { 2, 3 };
    final double[] result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                               start, lower, upper).getPoint();
    Assert.assertTrue("Out of lower bound (" + result[0] + " < " + lower[0] + ")",
                      result[0] >= lower[0]);
    Assert.assertTrue("Out of upper bound (" + result[0] + " > " + upper[0] + ")",
                      result[0] <= upper[0]);
    Assert.assertTrue("Out of lower bound (" + result[1] + " < " + lower[1] + ")",
                      result[1] >= lower[1]);
    Assert.assertTrue("Out of upper bound (" + result[1] + " > " + upper[1] + ")",
                      result[1] <= upper[1]);
}