// org/apache/commons/math/optimization/univariate/BrentOptimizerTest.java
@Test
    public void testSinMax() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer maximizer = new BrentOptimizer();
        double res = maximizer.optimize(f, GoalType.MAXIMIZE, 0, 4);
        assertEquals(Math.PI / 2, res, 10 * maximizer.getRelativeAccuracy());
    }