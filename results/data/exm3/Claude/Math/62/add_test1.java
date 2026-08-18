// org/apache/commons/math/optimization/univariate/MultiStartUnivariateRealOptimizerTest.java
@Test
    public void testQuinticMaxWithStartValue() throws MathException {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        underlying.setMaxEvaluations(300);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(1234567890L);
        MultiStartUnivariateRealOptimizer optimizer =
            new MultiStartUnivariateRealOptimizer(underlying, 3, g);

        UnivariateRealPointValuePair optimum
            = optimizer.optimize(f, GoalType.MAXIMIZE, 0.2, 0.4, 0.3);
        assertEquals(0.27195613, optimum.getPoint(), 1e-7);
        assertEquals(0.0443342695, optimum.getValue(), 1e-9);
    }