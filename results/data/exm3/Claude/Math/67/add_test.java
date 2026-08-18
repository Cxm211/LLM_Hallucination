// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java
@Test
public void testQuinticMax() throws MathException {
    UnivariateRealFunction f = new QuinticFunction();
    UnivariateRealOptimizer underlying = new BrentOptimizer();
    JDKRandomGenerator g = new JDKRandomGenerator();
    g.setSeed(4312000054l);
    MultiStartUnivariateRealOptimizer maximizer =
        new MultiStartUnivariateRealOptimizer(underlying, 5, g);
    maximizer.setAbsoluteAccuracy(10 * maximizer.getAbsoluteAccuracy());
    maximizer.setRelativeAccuracy(10 * maximizer.getRelativeAccuracy());

    assertEquals(0.27195612846834, maximizer.optimize(f, GoalType.MAXIMIZE, 0.2, 0.3), 1.0e-13);
    assertEquals(0.27195612846834, maximizer.getResult(), 1.0e-13);
    assertEquals(0.04433426954946, maximizer.getFunctionValue(), 1.0e-13);

    double[] optima = maximizer.getOptima();
    double[] optimaValues = maximizer.getOptimaValues();
    for (int i = 0; i < optima.length; ++i) {
        assertEquals(f.value(optima[i]), optimaValues[i], 1.0e-10);
    }
}