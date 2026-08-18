// org/apache/commons/math3/optim/nonlinear/vector/jacobian/AbstractLeastSquaresOptimizerAbstractTest.java
@Test
public void testGetIterationsConvergesQuickly() {
    AbstractLeastSquaresOptimizer optim = createOptimizer();
    optim.optimize(
        new MaxEval(10),
        new Target(new double[] { 2 }),
        new Weight(new double[] { 1 }),
        new InitialGuess(new double[] { 1.9 }),
        new ModelFunction(new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }
        }),
        new ModelFunctionJacobian(new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) {
                return new double[][] { { 1.0 } };
            }
        }));
    Assert.assertTrue(optim.getIterations() > 0);
}