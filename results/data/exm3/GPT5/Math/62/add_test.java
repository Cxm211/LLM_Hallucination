// org/apache/commons/math/optimization/univariate/MultiStartUnivariateRealOptimizerTest.java::testQuinticMinWithStartValue
@Test
public void testQuinticMinWithStartValue() throws MathException {
    UnivariateRealFunction f = new QuinticFunction();
    UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
    underlying.setMaxEvaluations(300);
    JDKRandomGenerator g = new JDKRandomGenerator();
    g.setSeed(4312000053L);
    MultiStartUnivariateRealOptimizer optimizer =
        new MultiStartUnivariateRealOptimizer(underlying, 5, g);

    // Provide a start value inside the interval; behavior should mirror the 4-arg optimize.
    UnivariateRealPointValuePair optimum
        = optimizer.optimize(f, GoalType.MINIMIZE, -0.3, -0.2, -0.25);
    assertEquals(-0.2719561293, optimum.getPoint(), 1e-9);
    assertEquals(-0.0443342695, optimum.getValue(), 1e-9);

    UnivariateRealPointValuePair[] optima = optimizer.getOptima();
    for (int i = 0; i < optima.length; ++i) {
        assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1e-9);
    }
    assertTrue(optimizer.getEvaluations() >= 50);
    assertTrue(optimizer.getEvaluations() <= 100);
}