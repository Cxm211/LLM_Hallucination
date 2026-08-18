// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java
@Test
public void testBoundariesWithVerySmallRange() {
    final CMAESOptimizer optimizer = new CMAESOptimizer();
    final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                return (parameters[0] - 10.0) * (parameters[0] - 10.0);
            }
        };

    final double[] start = { 10.0 };
    final double[] lower = { 9.999999 };
    final double[] upper = { 10.000001 };
    
    PointValuePair result = optimizer.optimize(50000, fitnessFunction, GoalType.MINIMIZE,
                                               start, lower, upper);
    
    Assert.assertEquals(10.0, result.getPoint()[0], 1e-5);
}