// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java
@Test
public void testQuinticMaxMin() throws MathException {
    UnivariateRealFunction f = new QuinticFunction();
    UnivariateRealOptimizer optimizer = new BrentOptimizer();
    optimizer.setRelativeAccuracy(1e-10);
    optimizer.setAbsoluteAccuracy(1e-11);
    double resultMax = optimizer.optimize(f, GoalType.MAXIMIZE, 0.5, 1.0);
    assertEquals(0.82221643, resultMax, 1e-7);
    assertTrue(optimizer.getFunctionValue() > 0.08);
    double resultMin = optimizer.optimize(f, GoalType.MINIMIZE, -0.5, 0.0);
    assertEquals(-0.27195613, resultMin, 1e-7);
    assertTrue(optimizer.getFunctionValue() < -0.04);
}