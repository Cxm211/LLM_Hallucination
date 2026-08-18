// org/apache/commons/math/optimization/univariate/MultiStartUnivariateRealOptimizerTest.java
@Test(expected = ConvergenceException.class)
public void testStartValueOutsideBounds() throws MathException {
    UnivariateRealFunction f = new UnivariateRealFunction() {
        public double value(double x) {
            return x;
        }
    };
    UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
    underlying.setMaxEvaluations(100);
    JDKRandomGenerator g = new JDKRandomGenerator();
    g.setSeed(987654321L);
    MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 1, g);
    optimizer.optimize(f, GoalType.MINIMIZE, 0, 1, 2.0);
}
