// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
public void testRepairLowerBound() {
    final CMAESOptimizer optimizer = new CMAESOptimizer();
    final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            @Override
            public double value(double[] parameters) {
                final double target = -2;
                final double error = target - parameters[0];
                return error * error;
            }
        };

    final double[] start = { 0 };
    final double[] lower = { -0.5 };
    final double[] upper = { 1e6 };
    final double[] result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                               start, lower, upper).getPoint();
    Assert.assertTrue("Out of bounds (" + result[0] + " < " + lower[0] + ")",
                      result[0] >= lower[0]);
}