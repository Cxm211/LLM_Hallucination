// org/apache/commons/math/optimization/univariate/MultiStartUnivariateRealOptimizerTest.java
@Test
public void testStartValueWithLowEvaluations() throws MathException {
    UnivariateRealFunction f = new UnivariateRealFunction() {
        public double value(double x) {
            return Math.pow(x - 2.0, 2);
        }
    };
    UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
    underlying.setMaxEvaluations(5);
    JDKRandomGenerator g = new JDKRandomGenerator();
    g.setSeed(123456L);
    MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 1, g);
    UnivariateRealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, 0, 5, 2.1);
    assertEquals(2.0, optimum.getPoint(), 1e-6);
}
