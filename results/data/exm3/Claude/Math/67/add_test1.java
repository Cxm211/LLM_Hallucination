// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java
@Test
public void testSinMin() throws MathException {
    UnivariateRealFunction f = new SinFunction();
    UnivariateRealOptimizer underlying = new BrentOptimizer();
    JDKRandomGenerator g = new JDKRandomGenerator();
    g.setSeed(12345678l);
    MultiStartUnivariateRealOptimizer minimizer =
        new MultiStartUnivariateRealOptimizer(underlying, 3, g);

    assertEquals(-Math.PI/2, minimizer.optimize(f, GoalType.MINIMIZE, -2.0, 2.0), 1.0e-10);
    assertEquals(-Math.PI/2, minimizer.getResult(), 1.0e-10);
    assertEquals(-1.0, minimizer.getFunctionValue(), 1.0e-10);

    double[] optima = minimizer.getOptima();
    double[] optimaValues = minimizer.getOptimaValues();
    assertEquals(3, optima.length);
    assertEquals(3, optimaValues.length);
    for (int i = 0; i < optima.length; ++i) {
        assertEquals(f.value(optima[i]), optimaValues[i], 1.0e-10);
    }
}