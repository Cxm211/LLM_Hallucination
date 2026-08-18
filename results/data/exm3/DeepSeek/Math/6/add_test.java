// org/apache/commons/math3/optim/nonlinear/scalar/gradient/NonLinearConjugateGradientOptimizerTest.java
@Test
    public void testStationaryPoint() {
        for (NonLinearConjugateGradientOptimizer.Formula formula : 
             NonLinearConjugateGradientOptimizer.Formula.values()) {
            NonLinearConjugateGradientOptimizer optimizer = 
                new NonLinearConjugateGradientOptimizer(formula,
                                                        new SimpleValueChecker(1e-6, 1e-6));
            MultivariateFunction f = new MultivariateFunction() {
                public double value(double[] x) {
                    return Math.pow(x[0] - 1, 2);
                }
            };
            MultivariateVectorFunction g = new MultivariateVectorFunction() {
                public double[] value(double[] x) {
                    return new double[] { 2 * (x[0] - 1) };
                }
            };
            PointValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(f),
                new ObjectiveFunctionGradient(g),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0 })
            );
            Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-10);
            Assert.assertEquals(0.0, optimum.getValue(), 1e-10);
        }
    }
