// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
public void testBoundariesWithZeroRange() {
    final CMAESOptimizer optimizer = new CMAESOptimizer();
    final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                return (parameters[0] - 5) * (parameters[0] - 5);
            }
        };

    final double[] start = { 5 };
    final double[] lower = { 5 };
    final double[] upper = { 5 };
    
    PointValuePair result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                               start, lower, upper);
    
    Assert.assertEquals(5.0, result.getPoint()[0], 1e-10);
}