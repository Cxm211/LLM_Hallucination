// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java
@Test
public void testSinMax() throws MathException {
    UnivariateRealFunction f = new SinFunction();
    UnivariateRealOptimizer optimizer = new BrentOptimizer();
    optimizer.setMaxEvaluations(200);
    double result = optimizer.optimize(f, GoalType.MAXIMIZE, 0, Math.PI);
    assertEquals(Math.PI / 2, result, optimizer.getAbsoluteAccuracy());
    assertEquals(1.0, optimizer.getFunctionValue(), optimizer.getAbsoluteAccuracy());
    assertTrue(optimizer.getIterationCount() <= 50);
}