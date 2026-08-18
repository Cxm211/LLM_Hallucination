// org/apache/commons/math3/optim/nonlinear/scalar/noderiv/PowellOptimizerTest.java
@Test
    public void testIterationsUpdatedPowell() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-9, 1e-9);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] x) {
                double s = 0;
                for (double v : x) {
                    s += v * v;
                }
                return s;
            }
        };
        PointValuePair optimum = optimizer.optimize(new MaxEval(100),
                                                    new ObjectiveFunction(f),
                                                    GoalType.MINIMIZE,
                                                    new InitialGuess(new double[] { 1.0, -2.0 }));
        Assert.assertNotNull(optimum);
        Assert.assertTrue(optimizer.getIterations() > 0);
    }