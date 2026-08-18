// org/apache/commons/math/optimization/MultiStartUnivariateRealOptimizerTest.java::testQuinticMax
@Test
public void testQuinticMax() throws MathException {
    UnivariateRealFunction f = new QuinticFunction();
    UnivariateRealOptimizer underlying = new BrentOptimizer();
    JDKRandomGenerator g = new JDKRandomGenerator();
    g.setSeed(4312000053l);
    MultiStartUnivariateRealOptimizer maximizer =
        new MultiStartUnivariateRealOptimizer(underlying, 5, g);
    maximizer.setAbsoluteAccuracy(10 * maximizer.getAbsoluteAccuracy());
    maximizer.setRelativeAccuracy(10 * maximizer.getRelativeAccuracy());

    double res = maximizer.optimize(f, GoalType.MAXIMIZE, 0.7, 0.9);
    assertEquals(res, maximizer.getResult(), 1.0e-13);
    assertEquals(f.value(res), maximizer.getFunctionValue(), 1.0e-13);
}
